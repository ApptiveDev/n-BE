package masil.backend.modules.chat.exception;

import masil.backend.global.base.BaseExceptionType;
import org.springframework.http.HttpStatus;

public enum ChatExceptionType implements BaseExceptionType {
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 채팅방에 접근할 수 없습니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "메시지를 찾을 수 없습니다."),
    INVALID_MESSAGE_CONTENT(HttpStatus.BAD_REQUEST, "메시지 내용이 올바르지 않습니다."),
    INVALID_MESSAGE_LANGUAGE(HttpStatus.BAD_REQUEST, "메시지 언어가 올바르지 않습니다."),
    MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메시지 전송에 실패했습니다."),
    TRANSLATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "메시지 번역에 실패했습니다. 원문은 저장되었습니다."),
    WEBSOCKET_AUTH_FAILED(HttpStatus.UNAUTHORIZED, "WebSocket 인증에 실패했습니다."),
    CHAT_ROOM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 채팅방이 존재합니다.");
    
    private final HttpStatus httpStatus;
    private final String errorMessage;
    
    ChatExceptionType(HttpStatus httpStatus, String errorMessage) {
        this.httpStatus = httpStatus;
        this.errorMessage = errorMessage;
    }
    
    @Override
    public HttpStatus httpStatus() {
        return httpStatus;
    }
    
    @Override
    public String errorMessage() {
        return errorMessage;
    }
}
