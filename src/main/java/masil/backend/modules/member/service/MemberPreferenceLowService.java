package masil.backend.modules.member.service;

import static masil.backend.global.error.GeneralExceptionType.FAILED_TO_CONVERT_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import masil.backend.global.error.GeneralException;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberPreference;
import masil.backend.modules.member.enums.AppearanceStyle;
import masil.backend.modules.member.enums.EducationLevel;
import masil.backend.modules.member.enums.JobType;
import masil.backend.modules.member.enums.ParentAssetLevel;
import masil.backend.modules.member.enums.PreferenceCategory;
import masil.backend.modules.member.repository.MemberPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberPreferenceLowService {
    private final MemberPreferenceRepository memberPreferenceRepository;
    private final ObjectMapper objectMapper;

    public Optional<MemberPreference> findByMemberId(final Long memberId) {
        return memberPreferenceRepository.findByMemberId(memberId);
    }

    public String convertJobListToJson(final List<JobType> jobs) {
        if (jobs == null || jobs.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(jobs);
        } catch (JsonProcessingException e) {
            throw new GeneralException(FAILED_TO_CONVERT_JSON);
        }
    }

    @Transactional
    public void saveOrUpdateMemberPreference(
            final Member member,
            final Integer preferredHeightMin,
            final Integer preferredHeightMax,
            final Integer avoidReligionsBitmask,
            final EducationLevel preferredEducationLevel,
            final AppearanceStyle preferredAppearanceStyle,
            final ParentAssetLevel parentAssetRequirement,
            final Long preferredAssetMin,
            final Long preferredAssetMax,
            final String preferredJobsJson,
            final String avoidedJobsJson,
            final String mbti1,
            final String mbti2,
            final String mbti3,
            final String mbti4,
            final PreferenceCategory priority1,
            final PreferenceCategory priority2,
            final PreferenceCategory priority3
    ) {
        final Optional<MemberPreference> existingPreference = findByMemberId(member.getId());

        if (existingPreference.isPresent()) {
            // 수정
            existingPreference.get().updatePreference(
                    preferredHeightMin, preferredHeightMax,
                    avoidReligionsBitmask, preferredEducationLevel, preferredAppearanceStyle,
                    parentAssetRequirement, preferredAssetMin, preferredAssetMax,
                    preferredJobsJson, avoidedJobsJson,
                    mbti1, mbti2, mbti3, mbti4
            );
        } else {
            // 신규 저장
            final MemberPreference preference = new MemberPreference(
                    member,
                    preferredHeightMin, preferredHeightMax,
                    avoidReligionsBitmask, preferredEducationLevel, preferredAppearanceStyle,
                    parentAssetRequirement, preferredAssetMin, preferredAssetMax,
                    preferredJobsJson, avoidedJobsJson,
                    mbti1, mbti2, mbti3, mbti4,
                    priority1, priority2, priority3
            );
            memberPreferenceRepository.save(preference);
        }
    }
}
