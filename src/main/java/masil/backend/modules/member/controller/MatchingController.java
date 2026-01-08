package masil.backend.modules.member.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.global.security.annotation.LoginMember;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.modules.member.dto.response.FemaleMatchingListResponse;
import masil.backend.modules.member.dto.response.MalePendingMatchingResponse;
import masil.backend.modules.member.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/members/matchings")
public class MatchingController {
    
    private final MatchingService matchingService;
    

    //여성에게 매칭된 남성 목록 조회
    @GetMapping("/female")
    public ResponseEntity<List<FemaleMatchingListResponse>> getFemaleMatchingList(
            @LoginMember MemberDetails memberDetails
    ) {
        log.info("여성 매칭 목록 조회 요청: memberId={}", memberDetails.memberId());
        List<FemaleMatchingListResponse> matchings = matchingService.getFemaleMatchingList(memberDetails.memberId());
        return ResponseEntity.ok(matchings);
    }
    
    //여성이 남성 1명 선택
    @PostMapping("/female/{matchingId}/select")
    public ResponseEntity<Void> selectMaleByFemale(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long matchingId
    ) {
        log.info("여성이 남성 선택 요청: memberId={}, matchingId={}", memberDetails.memberId(), matchingId);
        matchingService.selectMaleByFemale(memberDetails.memberId(), matchingId);
        return ResponseEntity.ok().build();
    }

    //여성이 매칭 거절 (해당 여성의 모든 선택된 매칭을 거절)
    @PostMapping("/female/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatchingByFemale(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long matchingId
    ) {
        log.info("여성이 매칭 거절 요청: memberId={}, matchingId={}", memberDetails.memberId(), matchingId);
        matchingService.rejectMatchingByFemale(memberDetails.memberId(), matchingId);
        return ResponseEntity.ok().build();
    }

    //남성에게 대기 매칭 조회 (수락 대기 중인 매칭)
    @GetMapping("/male/pendingMatching")
    public ResponseEntity<List<MalePendingMatchingResponse>> getMalePendingMatchings(
            @LoginMember MemberDetails memberDetails
    ) {
        log.info("남성 대기 매칭 조회 요청: memberId={}", memberDetails.memberId());
        List<MalePendingMatchingResponse> pendingMatchings = matchingService.getMalePendingMatchings(memberDetails.memberId());
        return ResponseEntity.ok(pendingMatchings);
    }
    

    //남성이 매칭 수락
    @PostMapping("/male/{matchingId}/accept")
    public ResponseEntity<Void> acceptMatchingByMale(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long matchingId
    ) {
        log.info("남성이 매칭 수락 요청: memberId={}, matchingId={}", memberDetails.memberId(), matchingId);
        matchingService.acceptMatchingByMale(memberDetails.memberId(), matchingId);
        return ResponseEntity.ok().build();
    }
    
    //남성이 매칭 거절
    @PostMapping("/male/{matchingId}/reject")
    public ResponseEntity<Void> rejectMatchingByMale(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long matchingId
    ) {
        log.info("남성이 매칭 거절 요청: memberId={}, matchingId={}", memberDetails.memberId(), matchingId);
        matchingService.rejectMatchingByMale(memberDetails.memberId(), matchingId);
        return ResponseEntity.ok().build();
    }

    //여성이 선택한 매칭의 상태 조회 (수락 대기, 수락됨, 거절됨)

    @GetMapping("/female/selected")
    public ResponseEntity<List<FemaleMatchingListResponse>> getSelectedMatchingStatus(
            @LoginMember MemberDetails memberDetails
    ) {
        log.info("여성 선택 매칭 상태 조회 요청: memberId={}", memberDetails.memberId());
        List<FemaleMatchingListResponse> matchings = matchingService.getSelectedMatchingStatus(memberDetails.memberId());
        return ResponseEntity.ok(matchings);
    }
}

