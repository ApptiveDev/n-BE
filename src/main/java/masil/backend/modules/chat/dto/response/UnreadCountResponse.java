package masil.backend.modules.chat.dto.response;

import java.util.List;

public record UnreadCountResponse(
        Integer totalUnreadCount,
        List<UnreadCountByRoom> unreadCountByRoom
) {
    public record UnreadCountByRoom(
            Long chatRoomId,
            Integer unreadCount
    ) {}
    
    public static UnreadCountResponse of(
            Integer totalUnreadCount,
            List<UnreadCountByRoom> unreadCountByRoom
    ) {
        return new UnreadCountResponse(totalUnreadCount, unreadCountByRoom);
    }
}
