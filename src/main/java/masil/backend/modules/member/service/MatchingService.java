package masil.backend.modules.member.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.member.dto.response.FemaleMatchingListResponse;
import masil.backend.modules.member.dto.response.MalePendingMatchingResponse;
import masil.backend.modules.member.entity.Matching;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.enums.Gender;
import masil.backend.modules.member.enums.MatchingStatus;
import masil.backend.modules.member.enums.MemberStatus;
import masil.backend.modules.member.repository.MatchingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MatchingService {

    private final MatchingRepository matchingRepository;
    private final MemberLowService memberLowService;
    private final MemberImageLowService memberImageLowService;
    private final FcmService fcmService;


    //여성에게 매칭된 남성 목록 조회
    @Transactional(readOnly = true)
    public List<FemaleMatchingListResponse> getFemaleMatchingList(Long femaleMemberId) {
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);

        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭 목록을 조회할 수 있습니다.");
        }

        List<Matching> matchings = matchingRepository.findByFemaleMemberIdAndStatusOrderByMatchingOrder(
                femaleMemberId,
                MatchingStatus.PENDING_FEMALE_SELECTION
        );

        // 남성 회원들의 ID 추출
        List<Long> maleMemberIds = matchings.stream()
                .map(matching -> matching.getMaleMember().getId())
                .toList();

        // 남성 회원들의 이미지를 일괄 조회
        Map<Long, List<MemberImage>> memberImagesMap = maleMemberIds.stream()
                .collect(Collectors.toMap(
                        memberId -> memberId,
                        memberId -> memberImageLowService.findByMemberId(memberId)
                ));

        return matchings.stream()
                .map(matching -> FemaleMatchingListResponse.from(
                        matching,
                        memberImagesMap.get(matching.getMaleMember().getId())
                ))
                .toList();
    }

    //여성이 남성 1명 선택
    public void selectMaleByFemale(Long femaleMemberId, Long matchingId) {
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);

        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭을 선택할 수 있습니다.");
        }

        // 매칭 조회 및 검증
        Matching selectedMatching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        if (!selectedMatching.getFemaleMember().getId().equals(femaleMemberId)) {
            throw new IllegalArgumentException("본인의 매칭만 선택할 수 있습니다.");
        }

        if (selectedMatching.getStatus() != MatchingStatus.PENDING_FEMALE_SELECTION) {
            throw new IllegalArgumentException("선택 대기 중인 매칭만 선택할 수 있습니다.");
        }

        // 같은 여성의 다른 매칭들을 먼저 조회 (선택 전에 조회해야 함)
        List<Matching> otherMatchings = matchingRepository.findByFemaleMemberIdAndStatusOrderByMatchingOrder(
                femaleMemberId,
                MatchingStatus.PENDING_FEMALE_SELECTION
        );

        // 같은 여성의 다른 매칭들을 거절 상태로 변경
        otherMatchings.forEach(matching -> {
            if (!matching.getId().equals(matchingId)) {
                matching.reject();
            }
        });

        // 상태 변경 및 알림 전송 (항상 함께 호출되어야 함)
        changeStatusToPendingMaleAcceptance(selectedMatching, femaleMember);

        log.info("여성이 남성 선택: femaleMemberId={}, matchingId={}, selectedMaleId={}",
                femaleMemberId, matchingId, selectedMatching.getMaleMember().getId()
        );
    }

    //남성에게 대기 매칭 조회 (수락 대기 중인 매칭)
    @Transactional(readOnly = true)
    public List<MalePendingMatchingResponse> getMalePendingMatchings(Long maleMemberId) {
        // 남성 유저 검증
        Member maleMember = memberLowService.getValidateExistMemberById(maleMemberId);

        if (maleMember.getGender() != Gender.KOREAN_MALE) {
            throw new IllegalArgumentException("한국 남성 유저만 대기 매칭을 조회할 수 있습니다.");
        }

        // 수락 대기 중인 매칭 조회 (여성 정보 fetch join으로 함께 조회)
        List<Matching> matchings = matchingRepository.findByMaleMemberIdAndStatusWithFemaleMember(
                maleMemberId,
                MatchingStatus.PENDING_MALE_ACCEPTANCE
        );

        log.info("남성 대기 매칭 조회: maleMemberId={}, 수락대기 매칭 수={}", maleMemberId, matchings.size());

        // 매칭이 없으면 에러 반환 (여자가 아직 수락하지 않음)
        if (matchings.isEmpty()) {
            throw new IllegalArgumentException("대기 중인 매칭이 없습니다. 여성이 아직 선택하지 않았거나 이미 처리된 매칭입니다.");
        }

        // 여성 회원들의 ID 추출
        List<Long> femaleMemberIds = matchings.stream()
                .map(matching -> matching.getFemaleMember().getId())
                .toList();

        // 여성 회원들의 이미지를 일괄 조회
        Map<Long, List<MemberImage>> memberImagesMap = femaleMemberIds.stream()
                .collect(Collectors.toMap(
                        memberId -> memberId,
                        memberId -> memberImageLowService.findByMemberId(memberId)
                ));

        return matchings.stream()
                .map(matching -> MalePendingMatchingResponse.from(
                        matching,
                        memberImagesMap.get(matching.getFemaleMember().getId())
                ))
                .toList();
    }

    //남성이 매칭 수락
    public void acceptMatchingByMale(Long maleMemberId, Long matchingId) {
        // 남성 유저 검증
        Member maleMember = memberLowService.getValidateExistMemberById(maleMemberId);

        if (maleMember.getGender() != Gender.KOREAN_MALE) {
            throw new IllegalArgumentException("한국 남성 유저만 매칭을 수락할 수 있습니다.");
        }

        // 매칭 조회 및 검증
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        if (!matching.getMaleMember().getId().equals(maleMemberId)) {
            throw new IllegalArgumentException("본인의 매칭만 수락할 수 있습니다.");
        }

        if (matching.getStatus() != MatchingStatus.PENDING_MALE_ACCEPTANCE) {
            throw new IllegalArgumentException("수락 대기 중인 매칭만 수락할 수 있습니다.");
        }

        // 같은 남성의 다른 매칭들을 먼저 조회 (수락 전에 조회해야 함)
        List<Matching> otherMatchings = matchingRepository.findByMaleMemberIdAndStatus(
                maleMemberId,
                MatchingStatus.PENDING_MALE_ACCEPTANCE
        );

        // 매칭 수락
        matching.acceptByMale();

        // 여성 Member를 명시적으로 조회 (Lazy Loading 문제 방지)
        Long femaleMemberId = matching.getFemaleMember().getId();
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);

        // 수락한 매칭의 남성과 여성 상태 변경: CONNECTING → CONNECTED
        log.info("매칭 수락 전 상태 확인: maleMemberId={}, maleStatus={}, femaleMemberId={}, femaleStatus={}", 
                maleMemberId, maleMember.getStatus(), femaleMemberId, femaleMember.getStatus());

        if (maleMember.getStatus() == MemberStatus.CONNECTING) {
            maleMember.changeStatus(MemberStatus.CONNECTED);
            log.info("남성 매칭 수락으로 인한 상태 변경: maleMemberId={}, CONNECTING → CONNECTED", maleMemberId);
        } else {
            log.warn("남성 상태가 CONNECTING이 아닙니다: maleMemberId={}, currentStatus={}", maleMemberId, maleMember.getStatus());
        }

        if (femaleMember.getStatus() == MemberStatus.CONNECTING) {
            femaleMember.changeStatus(MemberStatus.CONNECTED);
            log.info("남성 매칭 수락으로 인한 여성 상태 변경: femaleMemberId={}, CONNECTING → CONNECTED", femaleMemberId);
        } else {
            log.warn("여성 상태가 CONNECTING이 아닙니다: femaleMemberId={}, currentStatus={}", femaleMemberId, femaleMember.getStatus());
        }

        // 같은 남성의 다른 매칭들을 거절 상태로 변경하고, 해당 여성들의 상태도 재매칭 가능하도록 변경
        otherMatchings.forEach(otherMatching -> {
            if (!otherMatching.getId().equals(matchingId)) {
                otherMatching.reject();
                
                // 거절된 매칭의 여성 상태 변경: CONNECTING → APPROVED (재매칭 가능하도록)
                Member otherFemaleMember = otherMatching.getFemaleMember();
                if (otherFemaleMember.getStatus() == MemberStatus.CONNECTING) {
                    otherFemaleMember.changeStatus(MemberStatus.APPROVED);
                    log.info("남성 매칭 수락으로 인한 다른 여성 상태 변경: femaleMemberId={}, CONNECTING → APPROVED", otherFemaleMember.getId());
                }
            }
        });

        log.info("남성이 매칭 수락 완료: maleMemberId={}, matchingId={}, femaleMemberId={}, 거절된 매칭 수={}",
                maleMemberId, matchingId, femaleMember.getId(),
                otherMatchings.size() - 1);
    }

    //남성이 매칭 거절
    public void rejectMatchingByMale(Long maleMemberId, Long matchingId) {
        // 남성 유저 검증
        Member maleMember = memberLowService.getValidateExistMemberById(maleMemberId);

        if (maleMember.getGender() != Gender.KOREAN_MALE) {
            throw new IllegalArgumentException("한국 남성 유저만 매칭을 거절할 수 있습니다.");
        }

        // 매칭 조회 및 검증
        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> new IllegalArgumentException("매칭을 찾을 수 없습니다."));

        if (!matching.getMaleMember().getId().equals(maleMemberId)) {
            throw new IllegalArgumentException("본인의 매칭만 거절할 수 있습니다.");
        }

        if (matching.getStatus() != MatchingStatus.PENDING_MALE_ACCEPTANCE) {
            throw new IllegalArgumentException("수락 대기 중인 매칭만 거절할 수 있습니다.");
        }

        // 매칭 거절
        matching.reject();

        // 남성 상태 변경: CONNECTING → APPROVED (재매칭 가능하도록)
        if (maleMember.getStatus() == MemberStatus.CONNECTING) {
            maleMember.changeStatus(MemberStatus.APPROVED);
            log.info("남성 매칭 거절로 인한 상태 변경: maleMemberId={}, CONNECTING → APPROVED", maleMemberId);
        }

        // 여성 상태 변경: CONNECTING → APPROVED (재매칭 가능하도록)
        Member femaleMember = matching.getFemaleMember();
        if (femaleMember.getStatus() == MemberStatus.CONNECTING) {
            femaleMember.changeStatus(MemberStatus.APPROVED);
            log.info("남성 매칭 거절로 인한 여성 상태 변경: femaleMemberId={}, CONNECTING → APPROVED", femaleMember.getId());
        }

        log.info("남성이 매칭 거절: maleMemberId={}, matchingId={}, femaleMemberId={}",
                maleMemberId, matchingId, matching.getFemaleMember().getId());
    }

    //여성이 선택한 매칭의 상태 조회 (수락 대기, 수락됨, 거절됨)
    @Transactional(readOnly = true)
    public List<FemaleMatchingListResponse> getSelectedMatchingStatus(Long femaleMemberId) {
        // 여성 유저 검증
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);

        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭 상태를 조회할 수 있습니다.");
        }

        // 선택된 매칭 조회 (수락 대기, 수락됨, 거절됨 상태)
        List<MatchingStatus> statuses = List.of(
                MatchingStatus.PENDING_MALE_ACCEPTANCE,
                MatchingStatus.ACCEPTED,
                MatchingStatus.REJECTED
        );

        List<Matching> matchings = matchingRepository.findByFemaleMemberIdAndStatusIn(
                femaleMemberId,
                statuses
        );

        log.info("여성 선택 매칭 상태 조회: femaleMemberId={}, 매칭 수={}", femaleMemberId, matchings.size());

        // 남성 회원들의 ID 추출
        List<Long> maleMemberIds = matchings.stream()
                .map(matching -> matching.getMaleMember().getId())
                .toList();

        // 남성 회원들의 이미지를 일괄 조회
        Map<Long, List<MemberImage>> memberImagesMap = maleMemberIds.stream()
                .collect(Collectors.toMap(
                        memberId -> memberId,
                        memberId -> memberImageLowService.findByMemberId(memberId)
                ));

        return matchings.stream()
                .map(matching -> FemaleMatchingListResponse.from(
                        matching,
                        memberImagesMap.get(matching.getMaleMember().getId())
                ))
                .toList();
    }

    //여성이 매칭 거절 (해당 여성의 모든 매칭 거절)
    public void rejectMatchingByFemale(Long femaleMemberId) {
        // 여성 유저 검증
        Member femaleMember = memberLowService.getValidateExistMemberById(femaleMemberId);

        if (femaleMember.getGender() != Gender.JAPANESE_FEMALE) {
            throw new IllegalArgumentException("일본 여성 유저만 매칭을 거절할 수 있습니다.");
        }

        // 해당 여성의 모든 매칭 조회 (선택 대기, 수락 대기, 수락됨 상태)
        List<MatchingStatus> statuses = List.of(
                MatchingStatus.PENDING_FEMALE_SELECTION,
                MatchingStatus.PENDING_MALE_ACCEPTANCE,
                MatchingStatus.ACCEPTED
        );
        
        List<Matching> allMatchings = matchingRepository.findByFemaleMemberIdAndStatusIn(
                femaleMemberId,
                statuses
        );

        if (allMatchings.isEmpty()) {
            log.info("거절할 매칭이 없습니다: femaleMemberId={}", femaleMemberId);
            return;
        }

        log.info("여성이 매칭 거절 시작: femaleMemberId={}, 거절할 매칭 수={}", femaleMemberId, allMatchings.size());

        // 모든 매칭 거절 및 각 남성의 상태 변경
        for (Matching matching : allMatchings) {
            // 매칭 거절
            matching.reject();
            
            // 해당 남성의 상태 변경: CONNECTING → APPROVED (재매칭 가능하도록)
            Member maleMember = matching.getMaleMember();
            if (maleMember.getStatus() == MemberStatus.CONNECTING) {
                maleMember.changeStatus(MemberStatus.APPROVED);
                log.info("여성 매칭 거절로 인한 남성 상태 변경: maleMemberId={}, CONNECTING → APPROVED", maleMember.getId());
            }
            
            log.info("매칭 거절 완료: matchingId={}, maleMemberId={}", matching.getId(), maleMember.getId());
        }

        // 여성 상태 변경: CONNECTING → APPROVED (재매칭 가능하도록)
        if (femaleMember.getStatus() == MemberStatus.CONNECTING) {
            femaleMember.changeStatus(MemberStatus.APPROVED);
            log.info("여성 매칭 거절로 인한 상태 변경: femaleMemberId={}, CONNECTING → APPROVED", femaleMemberId);
        }

        log.info("여성이 모든 매칭 거절 완료: femaleMemberId={}, 총 거절된 매칭 수={}", femaleMemberId, allMatchings.size());
    }

    //매칭 상태를 PENDING_MALE_ACCEPTANCE로 변경하고 알림 전송 메서드
    private void changeStatusToPendingMaleAcceptance(Matching matching, Member femaleMember) {
        // 상태 변경
        matching.selectByFemale();
        // 상태 변경 후 알림 전송 (항상 함께 실행됨)
        sendNotificationForPendingMaleAcceptance(matching, femaleMember);
    }

    //PENDING_MALE_ACCEPTANCE 상태로 변경된 매칭에 대해 남성에게 푸시 알림 전송
    private void sendNotificationForPendingMaleAcceptance(Matching matching, Member femaleMember) {
        // 상태 확인 (안전장치)
        if (matching.getStatus() != MatchingStatus.PENDING_MALE_ACCEPTANCE) {
            log.warn("매칭 상태가 PENDING_MALE_ACCEPTANCE가 아닙니다. 알림을 전송하지 않습니다. matchingId={}, status={}",
                    matching.getId(), matching.getStatus());
            return;
        }

        Member maleMember = matching.getMaleMember();
        if (maleMember.getFcmToken() != null && !maleMember.getFcmToken().isBlank()) {
            String title = "매칭 알림";
            String body = String.format("%s님이 당신을 선택했습니다. 수락하시겠습니까?", femaleMember.getName());
            fcmService.sendPushNotification(maleMember.getFcmToken(), title, body);
            log.info("매칭 상태 변경 알림 전송: matchingId={}, status={}, maleMemberId={}, femaleMemberId={}",
                    matching.getId(), matching.getStatus(), maleMember.getId(), femaleMember.getId());
        } else {
            log.warn("FCM 토큰이 없어 알림을 전송할 수 없습니다: maleMemberId={}", maleMember.getId());
        }
    }
}
