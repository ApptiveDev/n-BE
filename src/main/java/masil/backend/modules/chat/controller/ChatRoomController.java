package masil.backend.modules.chat.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.global.security.annotation.LoginMember;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.modules.chat.dto.request.SendMessageRequest;
import masil.backend.modules.chat.dto.response.*;
import masil.backend.modules.chat.service.ChatMessageService;
import masil.backend.modules.chat.service.ChatRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatRoomController {
    
    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    
    /**
     * 채팅방 목록 조회
     */
    @GetMapping("/rooms")
    public ResponseEntity<ChatRoomListWrapperResponse> getChatRoomList(
            @LoginMember MemberDetails memberDetails
    ) {
        log.info("채팅방 목록 조회 요청: memberId={}", memberDetails.memberId());
        var chatRooms = chatRoomService.getChatRoomList(memberDetails.memberId());
        return ResponseEntity.ok(ChatRoomListWrapperResponse.from(chatRooms));
    }
    
    /**
     * 채팅방 상세 조회
     */
    @GetMapping("/rooms/{chatRoomId}")
    public ResponseEntity<ChatRoomDetailResponse> getChatRoomDetail(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long chatRoomId
    ) {
        log.info("채팅방 상세 조회 요청: memberId={}, chatRoomId={}", memberDetails.memberId(), chatRoomId);
        var response = chatRoomService.getChatRoomDetail(chatRoomId, memberDetails.memberId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 메시지 목록 조회 (페이징)
     */
    @GetMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<MessageListResponse> getMessages(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long chatRoomId,
            @PageableDefault(size = 20, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        log.info("메시지 목록 조회 요청: memberId={}, chatRoomId={}, page={}, size={}",
                memberDetails.memberId(), chatRoomId, pageable.getPageNumber(), pageable.getPageSize());
        
        Page<MessageResponse> messages = chatMessageService.getMessages(
                chatRoomId,
                memberDetails.memberId(),
                pageable
        );
        
        var response = MessageListResponse.from(
                messages.getContent(),
                messages.getNumber(),
                messages.getSize(),
                messages.getTotalElements(),
                messages.getTotalPages(),
                messages.hasNext(),
                messages.hasPrevious()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 읽지 않은 메시지 개수 조회
     */
    @GetMapping("/rooms/unread-count")
    public ResponseEntity<UnreadCountResponse> getUnreadCount(
            @LoginMember MemberDetails memberDetails
    ) {
        log.info("읽지 않은 메시지 개수 조회 요청: memberId={}", memberDetails.memberId());
        var response = chatMessageService.getUnreadCount(memberDetails.memberId());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 메시지 읽음 처리
     */
    @PutMapping("/rooms/{chatRoomId}/messages/read")
    public ResponseEntity<ReadMessagesResponse> markMessagesAsRead(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long chatRoomId
    ) {
        log.info("메시지 읽음 처리 요청: memberId={}, chatRoomId={}", memberDetails.memberId(), chatRoomId);
        Integer readCount = chatMessageService.markMessagesAsRead(chatRoomId, memberDetails.memberId());
        return ResponseEntity.ok(ReadMessagesResponse.of(readCount));
    }
    
    /**
     * 메시지 전송 (REST API - WebSocket이 아닌 경우를 위한 엔드포인트)
     * 주의: 실제 구현에서는 WebSocket을 통해 전송하는 것을 권장합니다.
     */
    @PostMapping("/rooms/{chatRoomId}/messages")
    public ResponseEntity<MessageResponse> sendMessage(
            @LoginMember MemberDetails memberDetails,
            @PathVariable Long chatRoomId,
            @Valid @RequestBody SendMessageRequest request
    ) {
        log.info("메시지 전송 요청 (REST): memberId={}, chatRoomId={}", memberDetails.memberId(), chatRoomId);
        
        // chatRoomId 검증
        if (!request.chatRoomId().equals(chatRoomId)) {
            throw new IllegalArgumentException("요청 URL의 채팅방 ID와 요청 본문의 채팅방 ID가 일치하지 않습니다.");
        }
        
        var message = chatMessageService.sendMessage(request, memberDetails.memberId());
        return ResponseEntity.ok(MessageResponse.from(message));
    }
}
