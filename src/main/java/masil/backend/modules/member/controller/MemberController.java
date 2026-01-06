package masil.backend.modules.member.controller;

import lombok.RequiredArgsConstructor;
import masil.backend.global.security.annotation.LoginMember;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.modules.member.dto.response.MyAiSummaryResponse;
import masil.backend.modules.member.dto.response.MyProfileResponse;
import masil.backend.modules.member.dto.response.MyStatusResponse;
import masil.backend.modules.member.service.MemberHighService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {
    private final MemberHighService memberHighService;

    @GetMapping("/status")
    public ResponseEntity<MyStatusResponse> getMyStatus(
            @LoginMember MemberDetails memberDetails
    ) {
        final MyStatusResponse status = memberHighService.getMemberStatus(memberDetails.memberId());
        return ResponseEntity.ok(status);
    }

    @GetMapping("/ai-summary")
    public ResponseEntity<MyAiSummaryResponse> getMyAiSummary(
            @LoginMember MemberDetails memberDetails
    ) {
        final MyAiSummaryResponse aiSummary = memberHighService.getMemberAiSummary(memberDetails.memberId());
        return ResponseEntity.ok(aiSummary);
    }

    @GetMapping("/profile")
    public ResponseEntity<MyProfileResponse> getMyProfile(
            @LoginMember MemberDetails memberDetails
    ) {
        final MyProfileResponse profile = memberHighService.getMemberProfile(memberDetails.memberId());
        return ResponseEntity.ok(profile);
    }
}
