package masil.backend.modules.member.service;

import static masil.backend.modules.member.exception.MemberExceptionType.CANNOT_MATCH_PASSWORD;

import java.util.List;
import lombok.RequiredArgsConstructor;
import masil.backend.global.security.provider.JwtProvider;
import masil.backend.modules.member.dto.request.SignInRequest;
import masil.backend.modules.member.dto.request.SignUpRequest;
import masil.backend.modules.member.dto.response.MyAiSummaryResponse;
import masil.backend.modules.member.dto.response.MyProfileResponse;
import masil.backend.modules.member.dto.response.MyStatusResponse;
import masil.backend.modules.member.dto.response.SignInResponse;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.exception.MemberException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberHighService {
    private final MemberLowService memberLowService;
    private final MemberImageLowService memberImageLowService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    public void signUp(final SignUpRequest signUpRequest) {
        final String encodedPassword = passwordEncoder.encode(signUpRequest.password());

        memberLowService.checkIsDuplicateEmail(signUpRequest.email());

        memberLowService.saveLocalMember(signUpRequest.email(), encodedPassword);
    }

    public SignInResponse signIn(final SignInRequest signInRequest) {
        final Member member = memberLowService.getValidateExistMemberByEmail(signInRequest.email());
        checkCorrectPassword(member.getPassword(), signInRequest.password());

        member.updateFcmToken(signInRequest.fcmToken());

        final String token = getToken(member.getId(), member.getName());
        return new SignInResponse(member, token, member.getStatus());
    }

    public MyStatusResponse getMemberStatus(final Long memberId) {
        final Member member = memberLowService.getValidateExistMemberById(memberId);
        return new MyStatusResponse(member);
    }

    public MyAiSummaryResponse getMemberAiSummary(final Long memberId) {
        final Member member = memberLowService.getValidateExistMemberById(memberId);
        return new MyAiSummaryResponse(member);
    }

    @Transactional(readOnly = true)
    public MyProfileResponse getMemberProfile(final Long memberId) {
        final Member member = memberLowService.getValidateExistMemberById(memberId);
        final List<MemberImage> memberImages = memberImageLowService.findByMemberId(memberId);
        return new MyProfileResponse(member, memberImages);
    }

    public void withdrawMember(final Long memberId) {
        memberLowService.deleteMember(memberId);
    }

    private void checkCorrectPassword(final String savePassword, final String inputPassword) {
        if (!passwordEncoder.matches(inputPassword, savePassword)) {
            throw new MemberException(CANNOT_MATCH_PASSWORD);
        }
    }

    private String getToken(final Long memberId, final String name) {
        return jwtProvider.createToken(String.valueOf(memberId), name);
    }
}
