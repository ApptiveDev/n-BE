package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.enums.*;

import java.time.LocalDateTime;
import java.util.List;

public record MemberInfoResponse(
        Long memberId,
        Provider provider,
        String providerId,
        MemberStatus status,
        String name,
        String email,
        Gender gender,
        Integer height,
        Integer weight,
        String residenceArea,
        SmokingStatus smokingStatus,
        DrinkingFrequency drinkingFrequency,
        Religion religion,
        Education education,
        Asset asset,
        String otherInfo,
        String thumbnailImageUrl,
        List<String> profileImageUrls,
        String aiSummary,
        String aiSummaryJp,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MemberInfoResponse from(Member member, List<MemberImage> memberImages) {
        return new MemberInfoResponse(
                member.getId(),
                member.getProvider(),
                member.getProviderId(),
                member.getStatus(),
                member.getName(),
                member.getEmail(),
                member.getGender(),
                member.getHeight(),
                member.getWeight(),
                member.getResidenceArea(),
                member.getSmokingStatus(),
                member.getDrinkingFrequency(),
                member.getReligion(),
                member.getEducation(),
                member.getAsset(),
                member.getOtherInfo(),
                member.getThumbnailImageUrl(),
                memberImages.stream()
                        .map(MemberImage::getImageUrl)
                        .toList(),
                member.getAiSummary(),
                member.getAiSummaryJp(),
                member.getCreatedAt(),
                member.getUpdatedAt()
        );
    }
}

