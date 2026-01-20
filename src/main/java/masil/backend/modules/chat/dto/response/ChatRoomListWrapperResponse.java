package masil.backend.modules.chat.dto.response;

import java.util.List;

public record ChatRoomListWrapperResponse(
        List<ChatRoomListResponse> chatRooms
) {
    public static ChatRoomListWrapperResponse from(List<ChatRoomListResponse> chatRooms) {
        return new ChatRoomListWrapperResponse(chatRooms);
    }
}
