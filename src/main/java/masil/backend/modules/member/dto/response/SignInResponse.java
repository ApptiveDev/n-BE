package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.enums.MemberStatus;

public record SignInResponse(
        Long memberId,
        String token,
        MemberStatus status
) {
    public SignInResponse(final Member member, final String token, final MemberStatus status) {
        this(
                member.getId(),
                token,
                status
        );
    }
}
