package masil.backend.modules.chat.exception;

import masil.backend.global.base.BaseExceptionType;

public enum ChatExceptionType implements BaseExceptionType {
    CHAT_ROOM_NOT_FOUND(404, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(403, "해당 채팅방에 접근할 수 없습니다."),
    MESSAGE_NOT_FOUND(404, "메시지를 찾을 수 없습니다."),
    INVALID_MESSAGE_CONTENT(400, "메시지 내용이 올바르지 않습니다."),
    INVALID_MESSAGE_LANGUAGE(400, "메시지 언어가 올바르지 않습니다."),
    MESSAGE_SEND_FAILED(500, "메시지 전송에 실패했습니다."),
    TRANSLATION_FAILED(500, "메시지 번역에 실패했습니다. 원문은 저장되었습니다."),
    WEBSOCKET_AUTH_FAILED(401, "WebSocket 인증에 실패했습니다."),
    CHAT_ROOM_ALREADY_EXISTS(409, "이미 채팅방이 존재합니다.");
    
    private final int statusCode;
    private final String message;
    
    ChatExceptionType(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }
    
    @Override
    public int getStatusCode() {
        return statusCode;
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
