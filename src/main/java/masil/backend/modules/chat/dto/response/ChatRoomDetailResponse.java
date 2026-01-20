package masil.backend.modules.chat.dto.response;

import masil.backend.modules.chat.entity.ChatRoom;
import masil.backend.modules.member.entity.Member;

import java.time.LocalDateTime;

public record ChatRoomDetailResponse(
        Long chatRoomId,
        Long matchingId,
        PartnerInfo partner,
        LocalDateTime createdAt
) {
    public record PartnerInfo(
            Long memberId,
            String name,
            String thumbnailImageUrl,
            String gender
    ) {}
    
    public static ChatRoomDetailResponse from(ChatRoom chatRoom, Member partner) {
        PartnerInfo partnerInfo = new PartnerInfo(
                partner.getId(),
                partner.getName(),
                partner.getThumbnailImageUrl(),
                partner.getGender() != null ? partner.getGender().name() : null
        );
        
        return new ChatRoomDetailResponse(
                chatRoom.getId(),
                chatRoom.getMatching().getId(),
                partnerInfo,
                chatRoom.getCreatedAt()
        );
    }
}
