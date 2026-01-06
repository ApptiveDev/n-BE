package masil.backend.modules.adminMember.dto.response;

import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.enums.*;

import java.time.LocalDateTime;

public record AdminMemberDetailResponse(
        Long id,
        String name,
        String email,
        MemberStatus status,
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
        LocalDateTime createdAt
) {
    public static AdminMemberDetailResponse from(Member member) {
        return new AdminMemberDetailResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getStatus(),
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
                member.getCreatedAt()
        );
    }
}
