package masil.backend.modules.chat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SendMessageRequest(
        @NotNull(message = "채팅방 ID는 필수입니다.")
        Long chatRoomId,
        
        @NotBlank(message = "메시지 내용은 필수입니다.")
        @Size(max = 1000, message = "메시지는 최대 1000자까지 입력 가능합니다.")
        String content
) {
}
