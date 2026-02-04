package masil.backend.modules.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import masil.backend.global.base.BaseEntity;
import masil.backend.modules.chat.enums.MessageLanguage;
import masil.backend.modules.chat.enums.MessageType;
import masil.backend.modules.member.entity.Member;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_message", indexes = {
    @Index(name = "idx_chat_room_id_created_at", columnList = "chat_room_id, created_at"),
    @Index(name = "idx_sender_id", columnList = "sender_id"),
    @Index(name = "idx_is_read", columnList = "is_read"),
    @Index(name = "idx_archived_at", columnList = "archived_at"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_message SET deleted_at = NOW() WHERE id = ?")
public class ChatMessage extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private Member sender;
    
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MessageLanguage language;
    
    @Column(name = "translated_content", columnDefinition = "TEXT")
    private String translatedContent;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 20)
    private MessageType messageType;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private LocalDateTime readAt;
    
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    @Builder
    private ChatMessage(
            ChatRoom chatRoom,
            Member sender,
            String content,
            MessageLanguage language,
            String translatedContent,
            MessageType messageType
    ) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.language = language;
        this.translatedContent = translatedContent;
        this.messageType = messageType;
        this.isRead = false;
    }
    
    public static ChatMessage create(
            ChatRoom chatRoom,
            Member sender,
            String content,
            MessageLanguage language,
            MessageType messageType
    ) {
        return ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .content(content)
                .language(language)
                .messageType(messageType)
                .build();
    }
    
    public void updateTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }
    
    public void markAsRead() {
        if (!this.isRead) {
            this.isRead = true;
            this.readAt = LocalDateTime.now();
        }
    }
    
    public void archive() {
        if (this.archivedAt == null) {
            this.archivedAt = LocalDateTime.now();
        }
    }
}
