package masil.backend.modules.chat.exception;

import masil.backend.global.base.BaseException;
import masil.backend.global.base.BaseExceptionType;

public class ChatException extends BaseException {
    
    private final ChatExceptionType exceptionType;
    
    public ChatException(ChatExceptionType exceptionType) {
        super(exceptionType.errorMessage());
        this.exceptionType = exceptionType;
    }
    
    public ChatException(ChatExceptionType exceptionType, String message) {
        super(message);
        this.exceptionType = exceptionType;
    }
    
    @Override
    public BaseExceptionType exceptionType() {
        return exceptionType;
    }
}
