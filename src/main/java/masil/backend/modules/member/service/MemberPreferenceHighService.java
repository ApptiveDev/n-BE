package masil.backend.modules.member.service;

import static masil.backend.modules.member.exception.MemberExceptionType.MEMBER_PREFERENCES_NOT_FOUND;
import static masil.backend.modules.member.exception.MemberExceptionType.MEMBER_PRIORITY_DUPLICATE_ERROR;

import lombok.RequiredArgsConstructor;
import masil.backend.modules.member.dto.request.SaveMemberPreferenceRequest;
import masil.backend.modules.member.dto.response.MemberPreferenceResponse;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberPreference;
import masil.backend.modules.member.enums.MemberStatus;
import masil.backend.modules.member.enums.PreferenceCategory;
import masil.backend.modules.member.enums.Religion;
import masil.backend.modules.member.exception.MemberException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberPreferenceHighService {
    private final MemberPreferenceLowService memberPreferenceLowService;
    private final MemberLowService memberLowService;
    private final MemberAiService memberAiService;
    private final MemberImageLowService memberImageLowService;

    public void saveMemberPreference(
            final Long memberId,
            final SaveMemberPreferenceRequest request
    ) {
        final Member member = memberLowService.getValidateExistMemberById(memberId);

        memberLowService.updateMemberProfile(
                member,
                request.name(),
                request.gender(), request.height(), request.weight(),
                request.residenceArea(), request.smokingStatus(), request.drinkingFrequency(),
                request.religion(), request.education(), request.asset(),
                request.otherInfo(), request.thumbnailImageUrl()
        );

        memberImageLowService.saveImages(member, request.profileImageUrls());

        validatePriorityDuplication(request.priority1(), request.priority2(), request.priority3());

        final Integer avoidReligionsBitmask = Religion.toBitmask(request.avoidReligions());
        final String preferredJobsJson = memberPreferenceLowService.convertJobListToJson(request.preferredJobs());
        final String avoidedJobsJson = memberPreferenceLowService.convertJobListToJson(request.avoidedJobs());

        memberPreferenceLowService.saveMemberPreference(
                member,
                request.preferredHeightMin(), request.preferredHeightMax(),
                avoidReligionsBitmask, request.preferredEducationLevel(), request.preferredAppearanceStyle(),
                request.parentAssetRequirement(), request.preferredAssetMin(), request.preferredAssetMax(),
                preferredJobsJson, avoidedJobsJson,
                request.mbti1(), request.mbti2(), request.mbti3(), request.mbti4(),
                request.priority1(), request.priority2(), request.priority3()
        );

        member.changeStatus(MemberStatus.PENDING_APPROVAL);

        memberAiService.generateMemberSummary(memberId, request.otherInfo());
    }

    @Transactional(readOnly = true)
    public MemberPreferenceResponse getMemberPreference(final Long memberId) {
        final MemberPreference preference = memberPreferenceLowService.findByMemberId(memberId)
                .orElseThrow(() -> new MemberException(MEMBER_PREFERENCES_NOT_FOUND));

        return new MemberPreferenceResponse(preference);
    }

    private void validatePriorityDuplication(
            final PreferenceCategory priority1,
            final PreferenceCategory priority2,
            final PreferenceCategory priority3
    ) {
        if (priority1 == priority2 || priority2 == priority3 || priority1 == priority3) {
            throw new MemberException(MEMBER_PRIORITY_DUPLICATE_ERROR);
        }
    }
}
