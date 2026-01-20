package masil.backend.modules.chat.exception;

import masil.backend.global.base.BaseException;

public class ChatException extends BaseException {
    
    public ChatException(ChatExceptionType exceptionType) {
        super(exceptionType);
    }
}
