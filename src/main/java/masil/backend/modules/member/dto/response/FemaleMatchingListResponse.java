package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Matching;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.enums.MatchingStatus;

import java.util.List;

public record FemaleMatchingListResponse(
        Long matchingId,
        Long maleMemberId,
        String maleName,
        String maleEmail,
        Integer height,
        Integer weight,
        String residenceArea,
        String thumbnailImageUrl,
        List<String> profileImageUrls,
        Integer matchingOrder,
        MatchingStatus status
) {
    public static FemaleMatchingListResponse from(Matching matching, List<MemberImage> memberImages) {
        return new FemaleMatchingListResponse(
                matching.getId(),
                matching.getMaleMember().getId(),
                matching.getMaleMember().getName(),
                matching.getMaleMember().getEmail(),
                matching.getMaleMember().getHeight(),
                matching.getMaleMember().getWeight(),
                matching.getMaleMember().getResidenceArea(),
                matching.getMaleMember().getThumbnailImageUrl(),
                memberImages.stream()
                        .map(MemberImage::getImageUrl)
                        .toList(),
                matching.getMatchingOrder(),
                matching.getStatus()
        );
    }
}
