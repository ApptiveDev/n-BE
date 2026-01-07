package masil.backend.modules.member.dto.response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.entity.MemberPreference;
import masil.backend.modules.member.enums.AppearanceStyle;
import masil.backend.modules.member.enums.EducationLevel;
import masil.backend.modules.member.enums.JobType;
import masil.backend.modules.member.enums.ParentAssetLevel;
import masil.backend.modules.member.enums.Religion;

import java.util.Collections;
import java.util.List;

public record MemberPreferenceResponse(
        Integer preferredHeightMin,
        Integer preferredHeightMax,
        List<Religion> avoidReligions,
        EducationLevel preferredEducationLevel,
        AppearanceStyle preferredAppearanceStyle,
        ParentAssetLevel parentAssetRequirement,
        Long preferredAssetMin,
        Long preferredAssetMax,
        List<JobType> preferredJobs,
        List<JobType> avoidedJobs,
        String mbtiE,
        String mbtiN,
        String mbtiT,
        String mbtiJ,
        String thumbnailImageUrl,
        List<String> profileImageUrls
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public MemberPreferenceResponse(MemberPreference preference, Member member, List<MemberImage> memberImages) {
        this(
                preference.getPreferredHeightMin(),
                preference.getPreferredHeightMax(),
                Religion.fromBitmask(preference.getAvoidReligionsBitmask()),
                preference.getPreferredEducationLevel(),
                preference.getPreferredAppearanceStyle(),
                preference.getParentAssetRequirement(),
                preference.getPreferredAssetMin(),
                preference.getPreferredAssetMax(),
                parseJobList(preference.getPreferredJobs()),
                parseJobList(preference.getAvoidedJobs()),
                preference.getMbtiE(),
                preference.getMbtiN(),
                preference.getMbtiT(),
                preference.getMbtiJ(),
                member.getThumbnailImageUrl(),
                memberImages.stream()
                        .map(MemberImage::getImageUrl)
                        .toList()
        );
    }

    private static List<JobType> parseJobList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<JobType>>() {});
        } catch (JsonProcessingException e) {
            return Collections.emptyList();
        }
    }
}
