package masil.backend.modules.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.modules.chat.dto.response.ChatRoomDetailResponse;
import masil.backend.modules.chat.dto.response.ChatRoomListResponse;
import masil.backend.modules.chat.entity.ChatMessage;
import masil.backend.modules.chat.entity.ChatRoom;
import masil.backend.modules.chat.repository.ChatMessageRepository;
import masil.backend.modules.chat.repository.ChatRoomRepository;
import masil.backend.modules.chat.exception.ChatException;
import masil.backend.modules.chat.exception.ChatExceptionType;
import masil.backend.modules.member.entity.Matching;
import masil.backend.modules.member.entity.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    
    /**
     * 매칭 수락 시 채팅방 자동 생성
     * @param matching ACCEPTED 상태의 매칭
     * @return 생성된 채팅방
     */
    public ChatRoom createChatRoom(Matching matching) {
        // 이미 채팅방이 존재하는지 확인
        if (chatRoomRepository.existsByMatchingId(matching.getId())) {
            log.warn("이미 채팅방이 존재합니다: matchingId={}", matching.getId());
            return chatRoomRepository.findByMatchingId(matching.getId())
                    .orElseThrow(() -> new ChatException(ChatExceptionType.CHAT_ROOM_NOT_FOUND));
        }
        
        ChatRoom chatRoom = ChatRoom.create(matching);
        ChatRoom saved = chatRoomRepository.save(chatRoom);
        
        log.info("채팅방 생성 완료: chatRoomId={}, matchingId={}, femaleMemberId={}, maleMemberId={}",
                saved.getId(), matching.getId(), 
                matching.getFemaleMember().getId(), 
                matching.getMaleMember().getId());
        
        return saved;
    }
    
    /**
     * 채팅방 조회 (권한 검증 포함)
     */
    @Transactional(readOnly = true)
    public ChatRoom getChatRoomById(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new ChatException(ChatExceptionType.CHAT_ROOM_NOT_FOUND));
        
        // 권한 검증: 채팅방 참여자인지 확인
        Long femaleMemberId = chatRoom.getMatching().getFemaleMember().getId();
        Long maleMemberId = chatRoom.getMatching().getMaleMember().getId();
        
        if (!femaleMemberId.equals(memberId) && !maleMemberId.equals(memberId)) {
            throw new ChatException(ChatExceptionType.CHAT_ROOM_ACCESS_DENIED);
        }
        
        return chatRoom;
    }
    
    /**
     * 사용자의 채팅방 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getChatRoomList(Long memberId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberId(memberId);
        
        return chatRooms.stream()
                .map(chatRoom -> {
                    // 상대방 정보 추출
                    Member partner = getPartner(chatRoom, memberId);
                    
                    // 마지막 메시지 조회
                    ChatMessage lastMessage = chatMessageRepository
                            .findLastMessageByChatRoomId(chatRoom.getId())
                            .orElse(null);
                    
                    // 읽지 않은 메시지 개수 조회
                    Long unreadCount = chatMessageRepository
                            .countUnreadMessagesByChatRoomIdAndMemberId(chatRoom.getId(), memberId);
                    
                    return ChatRoomListResponse.from(
                            chatRoom,
                            partner,
                            lastMessage,
                            unreadCount.intValue()
                    );
                })
                .collect(Collectors.toList());
    }
    
    /**
     * 채팅방 상세 조회
     */
    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomId, Long memberId) {
        ChatRoom chatRoom = getChatRoomById(chatRoomId, memberId);
        Member partner = getPartner(chatRoom, memberId);
        
        return ChatRoomDetailResponse.from(chatRoom, partner);
    }
    
    /**
     * 채팅방의 상대방 정보 추출
     */
    private Member getPartner(ChatRoom chatRoom, Long memberId) {
        Long femaleMemberId = chatRoom.getMatching().getFemaleMember().getId();
        Long maleMemberId = chatRoom.getMatching().getMaleMember().getId();
        
        if (femaleMemberId.equals(memberId)) {
            return chatRoom.getMatching().getMaleMember();
        } else if (maleMemberId.equals(memberId)) {
            return chatRoom.getMatching().getFemaleMember();
        }
        
        throw new ChatException(ChatExceptionType.CHAT_ROOM_ACCESS_DENIED);
    }
}
