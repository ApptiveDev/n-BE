package masil.backend.modules.member.controller;

import lombok.RequiredArgsConstructor;
import masil.backend.global.security.annotation.LoginMember;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.modules.member.dto.response.MemberInfoResponse;
import masil.backend.modules.member.service.MemberHighService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    private final MemberHighService memberHighService;

    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> getMyInfo(
            @LoginMember MemberDetails memberDetails
    ) {
        final MemberInfoResponse memberInfo = memberHighService.getMemberInfo(memberDetails.memberId());
        return ResponseEntity.ok(memberInfo);
    }
}
