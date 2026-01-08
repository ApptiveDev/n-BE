package masil.backend.modules.member.controller;


@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {
    @GetMapping("/info")
    public ResponseEntity<MemberInfoResponse> getMyInfo(
            @LoginMember MemberDetails memberDetails
    ) {
        final MemberInfoResponse memberInfo = memberHighService.getMemberInfo(memberDetails.memberId());
        return ResponseEntity.ok(memberInfo);
    }
}
