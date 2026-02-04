package masil.backend.modules.chat.dto.response;

public record ReadMessagesResponse(
        String message,
        Integer readCount
) {
    public static ReadMessagesResponse of(Integer readCount) {
        return new ReadMessagesResponse("메시지 읽음 처리 완료", readCount);
    }
}
