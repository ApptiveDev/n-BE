package masil.backend.modules.member.dto.response;

import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.entity.MemberImage;
import masil.backend.modules.member.enums.*;

import java.util.List;

public record MyProfileResponse(
        String name,
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
        List<String> profileImageUrls
) {
    public MyProfileResponse(Member member, List<MemberImage> memberImages) {
        this(
                member.getName(),
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
                        .toList()
        );
    }
}
