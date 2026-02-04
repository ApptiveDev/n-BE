package masil.backend.modules.chat.repository;

import masil.backend.modules.chat.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.deletedAt IS NULL " +
           "ORDER BY cm.createdAt DESC")
    Page<ChatMessage> findByChatRoomIdOrderByCreatedAtDesc(
            @Param("chatRoomId") Long chatRoomId,
            Pageable pageable
    );
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.isRead = false " +
           "AND cm.deletedAt IS NULL " +
           "AND cm.sender.id != :memberId")
    List<ChatMessage> findUnreadMessagesByChatRoomIdAndMemberId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") Long memberId
    );
    
    @Query("SELECT COUNT(cm) FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.isRead = false " +
           "AND cm.deletedAt IS NULL " +
           "AND cm.sender.id != :memberId")
    Long countUnreadMessagesByChatRoomIdAndMemberId(
            @Param("chatRoomId") Long chatRoomId,
            @Param("memberId") Long memberId
    );
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id IN " +
           "(SELECT cr.id FROM ChatRoom cr WHERE cr.deletedAt IS NULL " +
           "AND (cr.matching.femaleMember.id = :memberId OR cr.matching.maleMember.id = :memberId)) " +
           "AND cm.isRead = false " +
           "AND cm.deletedAt IS NULL " +
           "AND cm.sender.id != :memberId")
    List<ChatMessage> findAllUnreadMessagesByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.createdAt < :threeMonthsAgo " +
           "AND cm.archivedAt IS NULL " +
           "AND cm.deletedAt IS NULL")
    List<ChatMessage> findMessagesToArchive(@Param("threeMonthsAgo") LocalDateTime threeMonthsAgo);
    
    @Query("SELECT cm FROM ChatMessage cm " +
           "WHERE cm.chatRoom.id = :chatRoomId " +
           "AND cm.deletedAt IS NULL " +
           "ORDER BY cm.createdAt DESC")
    Optional<ChatMessage> findLastMessageByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
