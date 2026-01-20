package masil.backend.modules.chat.dto.response;

import masil.backend.modules.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record MessageResponse(
        Long messageId,
        Long chatRoomId,
        Long senderId,
        String senderName,
        String content,
        String translatedContent,
        String language,
        String messageType,
        Boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
    public static MessageResponse from(ChatMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getSender().getId(),
                message.getSender().getName(),
                message.getContent(),
                message.getTranslatedContent(),
                message.getLanguage() != null ? message.getLanguage().name() : null,
                message.getMessageType() != null ? message.getMessageType().name() : null,
                message.getIsRead(),
                message.getReadAt(),
                message.getCreatedAt()
        );
    }
}
