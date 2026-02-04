package masil.backend.modules.chat.dto.response;

import masil.backend.modules.chat.entity.ChatRoom;
import masil.backend.modules.chat.entity.ChatMessage;
import masil.backend.modules.member.entity.Member;

import java.time.LocalDateTime;

public record ChatRoomListResponse(
        Long chatRoomId,
        Long matchingId,
        PartnerInfo partner,
        LastMessageInfo lastMessage,
        Integer unreadCount,
        LocalDateTime createdAt
) {
    public record PartnerInfo(
            Long memberId,
            String name,
            String thumbnailImageUrl,
            String gender
    ) {}
    
    public record LastMessageInfo(
            Long messageId,
            String content,
            String translatedContent,
            String language,
            Long senderId,
            String messageType,
            LocalDateTime createdAt
    ) {}
    
    public static ChatRoomListResponse from(
            ChatRoom chatRoom,
            Member partner,
            ChatMessage lastMessage,
            Integer unreadCount
    ) {
        PartnerInfo partnerInfo = new PartnerInfo(
                partner.getId(),
                partner.getName(),
                partner.getThumbnailImageUrl(),
                partner.getGender() != null ? partner.getGender().name() : null
        );
        
        LastMessageInfo lastMessageInfo = lastMessage != null ? new LastMessageInfo(
                lastMessage.getId(),
                lastMessage.getContent(),
                lastMessage.getTranslatedContent(),
                lastMessage.getLanguage() != null ? lastMessage.getLanguage().name() : null,
                lastMessage.getSender().getId(),
                lastMessage.getMessageType() != null ? lastMessage.getMessageType().name() : null,
                lastMessage.getCreatedAt()
        ) : null;
        
        return new ChatRoomListResponse(
                chatRoom.getId(),
                chatRoom.getMatching().getId(),
                partnerInfo,
                lastMessageInfo,
                unreadCount,
                chatRoom.getCreatedAt()
        );
    }
}
