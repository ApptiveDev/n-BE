package masil.backend.modules.chat.dto.response;

import java.util.List;

public record MessageListResponse(
        List<MessageResponse> messages,
        PageInfo page
) {
    public record PageInfo(
            Integer number,
            Integer size,
            Long totalElements,
            Integer totalPages,
            Boolean hasNext,
            Boolean hasPrevious
    ) {}
    
    public static MessageListResponse from(
            List<MessageResponse> messages,
            Integer pageNumber,
            Integer pageSize,
            Long totalElements,
            Integer totalPages,
            Boolean hasNext,
            Boolean hasPrevious
    ) {
        PageInfo pageInfo = new PageInfo(
                pageNumber,
                pageSize,
                totalElements,
                totalPages,
                hasNext,
                hasPrevious
        );
        
        return new MessageListResponse(messages, pageInfo);
    }
}
