package masil.backend.modules.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import masil.backend.global.base.BaseEntity;
import masil.backend.modules.member.entity.Matching;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room", indexes = {
    @Index(name = "idx_matching_id", columnList = "matching_id", unique = true),
    @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE chat_room SET deleted_at = NOW() WHERE id = ?")
public class ChatRoom extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false, unique = true)
    private Matching matching;
    
    @Column(name = "deleted_at")
    private java.time.LocalDateTime deletedAt;
    
    @Builder
    private ChatRoom(Matching matching) {
        this.matching = matching;
    }
    
    public static ChatRoom create(Matching matching) {
        return ChatRoom.builder()
                .matching(matching)
                .build();
    }
}
