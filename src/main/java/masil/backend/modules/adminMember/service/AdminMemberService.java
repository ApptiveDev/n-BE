package masil.backend.modules.adminMember.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.adminMember.dto.request.ChangeMemberStatusRequest;
import masil.backend.modules.adminMember.dto.request.CreateMatchingRequest;
import masil.backend.modules.adminMember.dto.response.AdminMemberDetailResponse;
import masil.backend.modules.adminMember.dto.response.AdminMemberListResponse;
import masil.backend.modules.member.dto.response.MatchingScoreResponse;
import masil.backend.modules.member.entity.Matching;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.enums.Gender;
import masil.backend.modules.member.enums.MemberStatus;
import masil.backend.modules.member.repository.MemberRepository;
import masil.backend.modules.member.service.FcmService;
import masil.backend.modules.member.service.MatchingScoreService;
import masil.backend.modules.member.service.MemberLowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import masil.backend.modules.member.dto.response.MatchedMemberListResponse;
import masil.backend.modules.member.repository.MatchingRepository;


import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AdminMemberService {
    
    private final MemberRepository memberRepository;
    private final MemberLowService memberLowService;
    private final MatchingScoreService matchingScoreService;
    private final MatchingRepository matchingRepository;
    private final FcmService fcmService;

    //Use Case 1: 승인 대기 상태 유저 목록 조회

    @Transactional(readOnly = true)
    public List<AdminMemberListResponse> getPendingApprovalMembers(String keyword) {
        List<Member> members;
        
        if (keyword != null && !keyword.isBlank()) {
            members = memberRepository.findByStatusAndKeyword(MemberStatus.PENDING_APPROVAL, keyword);
        } else {
            members = memberRepository.findByStatus(MemberStatus.PENDING_APPROVAL);
        }
        
        log.info("승인 대기 유저 조회: {}명 (keyword: {})", members.size(), keyword);
        
        return members.stream()
                .map(AdminMemberListResponse::from)
                .collect(Collectors.toList());
    }
    

    //Use Case 1: 유저 상세 정보 조회
    
    @Transactional(readOnly = true)
    public AdminMemberDetailResponse getMemberDetail(Long memberId) {
        Member member = memberLowService.getValidateExistMemberById(memberId);
        log.info("유저 상세 정보 조회: memberId={}", memberId);
        return AdminMemberDetailResponse.from(member);
    }
    

    //Use Case 2: 승인대기 유저 상태 변경
    // 승인대기 → 승인완료료 또는 블랙유저

    public void changeMemberStatus(Long memberId, ChangeMemberStatusRequest request) {
        Member member = memberLowService.getValidateExistMemberById(memberId);
        
        // 승인 대기 상태가 아니면 에러
        if (member.getStatus() != MemberStatus.PENDING_APPROVAL) {
            throw new IllegalArgumentException("승인 대기 상태의 유저만 상태를 변경할 수 있습니다.");
        }
        
        // 연결중 또는 블랙유저로만 변경 가능
        if (request.status() != MemberStatus.APPROVED && request.status() != MemberStatus.BLACKLISTED) {
            throw new IllegalArgumentException("승인 대기 상태의 유저는 '연결중' 또는 '블랙유저' 상태로만 변경할 수 있습니다.");
        }
        
        member.changeStatus(request.status());

        // 푸시 알림 전송
        String title = "회원 상태 변경 알림";
        String body = getStatusChangeMessage(request.status());
        fcmService.sendPushNotification(member.getFcmToken(), title, body);
    }

    //Use Case 3: 승인완료료 상태 여성 유저 목록 조회

    @Transactional(readOnly = true)
    public List<AdminMemberListResponse> getConnectingFemaleMembers() {
        List<Member> members = memberRepository.findByGenderAndStatus(
                Gender.JAPANESE_FEMALE, 
                MemberStatus.APPROVED
        );
                
        return members.stream()
                .map(AdminMemberListResponse::from)
                .collect(Collectors.toList());
    }
    

    //Use Case 4: 여성 유저 기준으로 매칭 가능한 남성 유저 목록 조회 (점수 내림차순)

    @Transactional(readOnly = true)
    public List<MatchingScoreResponse> getMatchingCandidates(Long femaleMemberId) {
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);
        
        // 여성 유저가 연결중 상태인지 확인
        if (femaleMember.getStatus() != MemberStatus.APPROVED) {
            throw new IllegalArgumentException("연결중 상태의 여성 유저만 매칭 후보를 조회할 수 있습니다.");
        }
        
        // 여성이 맞는지 확인
        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭 후보를 조회할 수 있습니다.");
        }
        
        // APPROVED 또는 CONNECTING 상태 남성 유저 조회 (이미 매칭된 남성도 포함)
        List<Member> maleMembers = memberRepository.findByGenderAndStatusIn(
                Gender.KOREAN_MALE,
                List.of(MemberStatus.APPROVED, MemberStatus.CONNECTING)
        );
        
        log.info("매칭 후보 조회: 여성 memberId={}, 남성 후보 수={} (APPROVED 및 CONNECTING 상태)", 
                femaleMemberId, maleMembers.size());
        
        // 매칭 점수 계산 및 정렬
        return maleMembers.stream()
        .map(male -> {
            Double score = matchingScoreService.calculateMatchingScore(femaleMember, male);
            
            // 해당 남성의 매칭 정보 조회
            List<Matching> matchings = matchingRepository.findByMaleMemberId(male.getId());
            int matchingCount = matchings.size();
            return MatchingScoreResponse.from(male, score, matchingCount);
        })
        .sorted((a, b) -> Double.compare(b.matchingScore(), a.matchingScore())) // 내림차순
        .collect(Collectors.toList());
    }
    

    //Use Case 5: 최종 매칭 생성 (여성 1명 + 남성 3명을 연결됨 상태로 변경)

    public void createMatching(CreateMatchingRequest request) {
        // 여성 유저 조회 및 검증
        Member femaleMember = memberLowService.getValidateExistMemberById(request.femaleMemberId());
        
        if (femaleMember.getStatus() != MemberStatus.APPROVED) {
            throw new IllegalArgumentException("연결중 상태의 여성 유저만 매칭할 수 있습니다.");
        }
        
        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭할 수 있습니다.");
        }
        
        // 남성 유저들 조회 및 검증
        if (request.maleMemberIds().size() != 3) {
            throw new IllegalArgumentException("남성 유저는 정확히 3명을 선택해야 합니다.");
        }
        
        List<Member> maleMembers = request.maleMemberIds().stream()
                .map(memberLowService::getValidateExistMemberById)
                .peek(member -> {
                    if (member.getStatus() != MemberStatus.APPROVED && member.getStatus() != MemberStatus.CONNECTING) {
                        throw new IllegalArgumentException(
                                String.format("선택한 유저 중 매칭 불가 상태가 있습니다. (memberId: %d, 상태: %s)", 
                                        member.getId(), member.getStatus()));
                    }
                    if (member.getGender() != Gender.KOREAN_MALE) {
                        throw new IllegalArgumentException(
                                String.format("한국 남성만 매칭할 수 있습니다. (memberId: %d)", member.getId()));
                    }
                })
                .collect(Collectors.toList());
        
        // 중복 체크
        long distinctCount = maleMembers.stream()
                .map(Member::getId)
                .distinct()
                .count();
        
        if (distinctCount != 3) {
            throw new IllegalArgumentException("중복된 남성 유저가 선택되었습니다.");
        }
        
        // 상태 변경: 승인완료 -> 연결중
        femaleMember.changeToConnecting();
        maleMembers.forEach(member -> {
            if (member.getStatus() == MemberStatus.APPROVED) {
                member.changeToConnecting();
            }
        });        
        // 매칭 테이블에 기록 생성
        for (int i = 0; i < maleMembers.size(); i++) {
            Matching matching = Matching.builder()
                    .femaleMember(femaleMember)
                    .maleMember(maleMembers.get(i))
                    .matchingOrder(i + 1)
                    .build();
            matchingRepository.save(matching);
        }

        log.info("매칭 생성 완료: 여성 memberId={}, 남성 memberIds={}", 
                request.femaleMemberId(), 
                request.maleMemberIds());
    }
    //Use Case 6: 생성된 매칭 목록 조회
    @Transactional(readOnly = true)
    public List<MatchedMemberListResponse> getAllMatchings() {
        List<Long> femaleMemberIds = matchingRepository.findDistinctFemaleMemberIds();
        
        return femaleMemberIds.stream()
                .map(femaleId -> {
                    List<Matching> matchings = matchingRepository.findByFemaleMemberIdOrderByMatchingOrder(femaleId);
                    return MatchedMemberListResponse.from(matchings);
                })
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    private String getStatusChangeMessage(MemberStatus status) {
        return switch (status) {
            case INCOMPLETE_PROFILE -> "프로필을 완성해주세요.";
            case PENDING_APPROVAL -> "회원님의 프로필이 검토 중입니다.";
            case APPROVED -> "회원님의 가입이 승인되었습니다! 🎉";
            case CONNECTING -> "매칭이 진행 중입니다.";
            case CONNECTED -> "매칭이 완료되었습니다! 축하드립니다! 🎊";
            case BLACKLISTED -> "회원님의 계정이 제한되었습니다.";
        };
    }
}
