package masil.backend.modules.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.chat.dto.request.SendMessageRequest;
import masil.backend.modules.chat.dto.response.MessageResponse;
import masil.backend.modules.chat.dto.response.UnreadCountResponse;
import masil.backend.modules.chat.entity.ChatMessage;
import masil.backend.modules.chat.entity.ChatRoom;
import masil.backend.modules.chat.enums.MessageLanguage;
import masil.backend.modules.chat.enums.MessageType;
import masil.backend.modules.chat.repository.ChatMessageRepository;
import masil.backend.modules.chat.repository.ChatRoomRepository;
import masil.backend.modules.member.entity.Member;
import masil.backend.modules.member.enums.Gender;
import masil.backend.modules.member.service.MemberLowService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatMessageService {
    
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomService chatRoomService;
    private final MemberLowService memberLowService;
    private final TranslationService translationService;
    
    /**
     * 메시지 전송 (하이브리드 방식: 원문 즉시 저장, 번역은 비동기로 처리)
     */
    public ChatMessage sendMessage(SendMessageRequest request, Long senderId) {
        // 채팅방 조회 및 권한 검증
        ChatRoom chatRoom = chatRoomService.getChatRoomById(request.chatRoomId(), senderId);
        
        // 발신자 조회
        Member sender = memberLowService.getValidateExistMemberById(senderId);
        
        // 언어 자동 감지
        MessageLanguage language = detectLanguage(sender);
        
        // 메시지 생성 및 저장 (원문만 저장, 번역은 비동기로 처리)
        ChatMessage message = ChatMessage.create(
                chatRoom,
                sender,
                request.content(),
                language,
                MessageType.TEXT
        );
        
        ChatMessage saved = chatMessageRepository.save(message);
        
        log.info("메시지 전송: messageId={}, chatRoomId={}, senderId={}, language={}",
                saved.getId(), request.chatRoomId(), senderId, language);
        
        // 비동기 번역 처리 시작
        translateAndUpdateAsync(saved);
        
        return saved;
    }
    
    /**
     * 비동기 번역 처리 및 업데이트
     */
    @Async("translationTaskExecutor")
    private void translateAndUpdateAsync(ChatMessage message) {
        try {
            ChatRoom chatRoom = chatRoomRepository.findById(message.getChatRoom().getId())
                    .orElseThrow(() -> new IllegalArgumentException("채팅방을 찾을 수 없습니다."));
            
            Member partner = getPartner(chatRoom, message.getSender().getId());
            MessageLanguage targetLanguage = detectLanguage(partner);
            
            // 번역 수행 (타임아웃 설정: 최대 5초 대기)
            String translated = translationService.translateAsync(
                    message.getContent(),
                    message.getLanguage(),
                    targetLanguage
            ).get(5, TimeUnit.SECONDS); // 비동기 완료 대기 (타임아웃 5초)
            
            // 번역문 저장 및 업데이트
            message.updateTranslatedContent(translated);
            chatMessageRepository.save(message);
            
            log.info("번역 완료: messageId={}, sourceLanguage={}, targetLanguage={}",
                    message.getId(), message.getLanguage(), targetLanguage);
            
        } catch (TimeoutException e) {
            log.error("번역 타임아웃: messageId={}", message.getId(), e);
            // 타임아웃 시에도 원문은 이미 저장됨
        } catch (Exception e) {
            log.error("번역 실패: messageId={}", message.getId(), e);
            // 번역 실패해도 원문은 이미 저장됨
            // 필요 시 재시도 큐에 추가하거나 관리자에게 알림
        }
    }
    
    /**
     * 채팅방의 상대방 정보 추출
     */
    private Member getPartner(ChatRoom chatRoom, Long senderId) {
        Long femaleMemberId = chatRoom.getMatching().getFemaleMember().getId();
        Long maleMemberId = chatRoom.getMatching().getMaleMember().getId();
        
        if (femaleMemberId.equals(senderId)) {
            return chatRoom.getMatching().getMaleMember();
        } else if (maleMemberId.equals(senderId)) {
            return chatRoom.getMatching().getFemaleMember();
        }
        
        throw new IllegalArgumentException("발신자가 채팅방 참여자가 아닙니다.");
    }
    
    /**
     * 메시지 목록 조회 (페이징)
     */
    @Transactional(readOnly = true)
    public Page<MessageResponse> getMessages(Long chatRoomId, Long memberId, Pageable pageable) {
        // 채팅방 조회 및 권한 검증
        chatRoomService.getChatRoomById(chatRoomId, memberId);
        
        // 메시지 조회 (최근 메시지부터)
        Page<ChatMessage> messages = chatMessageRepository.findByChatRoomIdOrderByCreatedAtDesc(
                chatRoomId, 
                pageable
        );
        
        return messages.map(MessageResponse::from);
    }
    
    /**
     * 읽지 않은 메시지 개수 조회
     */
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long memberId) {
        List<ChatMessage> unreadMessages = chatMessageRepository.findAllUnreadMessagesByMemberId(memberId);
        
        // 채팅방별로 그룹화
        var unreadCountByRoom = unreadMessages.stream()
                .collect(Collectors.groupingBy(
                        message -> message.getChatRoom().getId(),
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .map(entry -> new UnreadCountResponse.UnreadCountByRoom(
                        entry.getKey(),
                        entry.getValue().intValue()
                ))
                .collect(Collectors.toList());
        
        Integer totalUnreadCount = unreadMessages.size();
        
        return UnreadCountResponse.of(totalUnreadCount, unreadCountByRoom);
    }
    
    /**
     * 메시지 읽음 처리
     */
    public Integer markMessagesAsRead(Long chatRoomId, Long memberId) {
        // 채팅방 조회 및 권한 검증
        chatRoomService.getChatRoomById(chatRoomId, memberId);
        
        // 읽지 않은 메시지 조회
        List<ChatMessage> unreadMessages = chatMessageRepository.findUnreadMessagesByChatRoomIdAndMemberId(
                chatRoomId,
                memberId
        );
        
        // 읽음 처리
        unreadMessages.forEach(ChatMessage::markAsRead);
        chatMessageRepository.saveAll(unreadMessages);
        
        log.info("메시지 읽음 처리: chatRoomId={}, memberId={}, 읽은 메시지 수={}",
                chatRoomId, memberId, unreadMessages.size());
        
        return unreadMessages.size();
    }
    
    /**
     * 발신자 성별 기반 언어 감지
     */
    private MessageLanguage detectLanguage(Member sender) {
        if (sender.getGender() == Gender.JAPANESE_FEMALE) {
            return MessageLanguage.JAPANESE;
        } else if (sender.getGender() == Gender.KOREAN_MALE) {
            return MessageLanguage.KOREAN;
        }
        throw new IllegalArgumentException("지원하지 않는 성별입니다: " + sender.getGender());
    }
    
    /**
     * 번역 대상 언어 결정
     */
    public MessageLanguage getTargetLanguage(MessageLanguage sourceLanguage) {
        if (sourceLanguage == MessageLanguage.KOREAN) {
            return MessageLanguage.JAPANESE;
        } else if (sourceLanguage == MessageLanguage.JAPANESE) {
            return MessageLanguage.KOREAN;
        }
        throw new IllegalArgumentException("지원하지 않는 언어입니다: " + sourceLanguage);
    }
}
