package masil.backend.modules.chat.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.modules.chat.dto.request.SendMessageRequest;
import masil.backend.modules.chat.dto.response.MessageResponse;
import masil.backend.modules.chat.entity.ChatMessage;
import masil.backend.modules.chat.service.ChatMessageService;
import masil.backend.modules.chat.service.ChatRoomService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    
    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 메시지 전송
     * Destination: /app/chat.send
     */
    @MessageMapping("/chat.send")
    @SendToUser("/queue/messages")
    public MessageResponse sendMessage(
            @Payload SendMessageRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        // 인증 정보에서 사용자 ID 추출
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        Long senderId = memberDetails.memberId();
        
        log.info("WebSocket 메시지 전송 요청: senderId={}, chatRoomId={}, content={}",
                senderId, request.chatRoomId(), request.content());
        
        // 메시지 저장 및 번역 처리 시작
        ChatMessage message = chatMessageService.sendMessage(request, senderId);
        
        // 발신자에게 즉시 응답 (원문만 포함)
        MessageResponse response = MessageResponse.from(message);
        
        // 상대방에게도 메시지 전송 (원문만)
        sendMessageToPartner(message, senderId, false);
        
        return response;
    }
    
    /**
     * 읽음 상태 업데이트
     * Destination: /app/chat.read
     */
    @MessageMapping("/chat.read")
    public void markAsRead(
            @Payload ReadRequest request,
            @AuthenticationPrincipal Authentication authentication
    ) {
        MemberDetails memberDetails = (MemberDetails) authentication.getPrincipal();
        Long memberId = memberDetails.memberId();
        
        log.info("WebSocket 읽음 처리 요청: memberId={}, chatRoomId={}",
                memberId, request.chatRoomId());
        
        // 읽음 처리
        Integer readCount = chatMessageService.markMessagesAsRead(
                request.chatRoomId(),
                memberId
        );
        
        // 상대방에게 읽음 상태 알림 전송
        sendReadStatusToPartner(request.chatRoomId(), memberId, readCount);
    }
    
    /**
     * 상대방에게 메시지 전송
     */
    private void sendMessageToPartner(ChatMessage message, Long senderId, boolean includeTranslation) {
        try {
            // 채팅방에서 상대방 ID 찾기
            Long partnerId = getPartnerId(message, senderId);
            
            // 메시지 응답 생성
            MessageResponse response = MessageResponse.from(message);
            
            // 상대방에게 전송
            messagingTemplate.convertAndSendToUser(
                    partnerId.toString(),
                    "/queue/messages",
                    response
            );
            
            log.debug("상대방에게 메시지 전송: partnerId={}, messageId={}, includeTranslation={}",
                    partnerId, message.getId(), includeTranslation);
            
        } catch (Exception e) {
            log.error("상대방에게 메시지 전송 실패: messageId={}", message.getId(), e);
        }
    }
    
    /**
     * 상대방에게 읽음 상태 알림 전송
     */
    private void sendReadStatusToPartner(Long chatRoomId, Long readerId, Integer readCount) {
        try {
            // 채팅방 조회 및 상대방 ID 찾기
            Long partnerId = getPartnerIdFromChatRoom(chatRoomId, readerId);
            
            // 읽음 상태 응답 생성
            ReadStatusResponse response = new ReadStatusResponse(
                    chatRoomId,
                    readCount
            );
            
            // 상대방에게 전송
            messagingTemplate.convertAndSendToUser(
                    partnerId.toString(),
                    "/queue/read-status",
                    response
            );
            
            log.debug("상대방에게 읽음 상태 알림 전송: partnerId={}, chatRoomId={}, readCount={}",
                    partnerId, chatRoomId, readCount);
            
        } catch (Exception e) {
            log.error("상대방에게 읽음 상태 알림 전송 실패: chatRoomId={}", chatRoomId, e);
        }
    }
    
    /**
     * 메시지에서 상대방 ID 추출
     */
    private Long getPartnerId(ChatMessage message, Long senderId) {
        return chatRoomService.getPartnerId(message.getChatRoom().getId(), senderId);
    }
    
    /**
     * 채팅방에서 상대방 ID 추출
     */
    private Long getPartnerIdFromChatRoom(Long chatRoomId, Long memberId) {
        return chatRoomService.getPartnerId(chatRoomId, memberId);
    }
    
    /**
     * 읽음 요청 DTO
     */
    public record ReadRequest(Long chatRoomId) {}
    
    /**
     * 읽음 상태 응답 DTO
     */
    public record ReadStatusResponse(Long chatRoomId, Integer readCount) {}
}
