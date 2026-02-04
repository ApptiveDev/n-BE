package masil.backend.modules.chat.repository;

import masil.backend.modules.chat.entity.ChatRoom;
import masil.backend.modules.member.entity.Matching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByMatchingId(Long matchingId);
    
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.deletedAt IS NULL " +
           "AND (cr.matching.femaleMember.id = :memberId OR cr.matching.maleMember.id = :memberId)")
    List<ChatRoom> findByMemberId(@Param("memberId") Long memberId);
    
    @Query("SELECT cr FROM ChatRoom cr " +
           "WHERE cr.deletedAt IS NULL " +
           "AND cr.matching.id = :matchingId")
    Optional<ChatRoom> findActiveByMatchingId(@Param("matchingId") Long matchingId);
    
    boolean existsByMatchingId(Long matchingId);
}
