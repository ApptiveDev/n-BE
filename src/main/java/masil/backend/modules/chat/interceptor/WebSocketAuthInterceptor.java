package masil.backend.modules.chat.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import masil.backend.global.security.dto.MemberDetails;
import masil.backend.global.security.provider.JwtProvider;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor, ChannelInterceptor {
    
    private final JwtProvider jwtProvider;
    
    private static final String TOKEN_HEADER = "Authorization";
    private static final String TOKEN_PARAM = "token";
    
    /**
     * WebSocket 핸드셰이크 전에 실행 (연결 시 인증)
     */
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        
        log.info("WebSocket 핸드셰이크 시작: token 존재 여부={}", token != null);
        
        if (token == null) {
            log.warn("WebSocket 인증 실패: 토큰이 없습니다.");
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false; // 연결 거부
        }
        
        if (!jwtProvider.validateToken(token)) {
            log.warn("WebSocket 인증 실패: 토큰 검증 실패. token prefix={}", 
                    token.length() > 20 ? token.substring(0, 20) + "..." : token);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false; // 연결 거부
        }
        
        try {
            // 인증 정보를 세션에 저장
            Authentication auth = jwtProvider.getAuthentication(token);
            attributes.put("authentication", auth);
            
            MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
            log.info("WebSocket 인증 성공: memberId={}", memberDetails.memberId());
            
            return true; // 연결 허용
        } catch (Exception e) {
            log.error("WebSocket 인증 중 오류 발생", e);
            response.setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return false;
        }
    }
    
    /**
     * WebSocket 핸드셰이크 후에 실행
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 완료 후 처리 (필요 시)
    }
    
    /**
     * STOMP 메시지 전송 전에 실행 (메시지 전송 시 인증 검증)
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            // CONNECT 프레임에서 토큰 추출 및 인증
            String token = extractTokenFromHeaders(accessor);
            
            if (token != null && jwtProvider.validateToken(token)) {
                Authentication auth = jwtProvider.getAuthentication(token);
                accessor.setUser(auth);
                MemberDetails memberDetails = (MemberDetails) auth.getPrincipal();
                log.info("STOMP CONNECT 인증 성공: memberId={}", memberDetails.memberId());
            } else {
                log.warn("STOMP CONNECT 인증 실패: token={}", token != null ? "present but invalid" : "null");
                throw new org.springframework.messaging.MessageDeliveryException("인증에 실패했습니다.");
            }
        }
        
        return message;
    }
    
    /**
     * 요청에서 토큰 추출 (Query Parameter 또는 Header)
     */
    private String extractToken(ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            
            // Query Parameter에서 토큰 추출
            String tokenParam = servletRequest.getServletRequest().getParameter(TOKEN_PARAM);
            if (tokenParam != null && !tokenParam.isEmpty()) {
                log.debug("토큰을 쿼리 파라미터에서 추출했습니다.");
                return tokenParam;
            }
            
            // Header에서 토큰 추출
            String authHeader = servletRequest.getServletRequest().getHeader(TOKEN_HEADER);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                log.debug("토큰을 헤더에서 추출했습니다.");
                return authHeader.substring(7);
            }
            
            log.warn("토큰을 찾을 수 없습니다. 쿼리 파라미터와 헤더를 모두 확인했습니다.");
        }
        
        return null;
    }
    
    /**
     * STOMP 헤더에서 토큰 추출
     */
    private String extractTokenFromHeaders(StompHeaderAccessor accessor) {
        // STOMP 헤더에서 Authorization 추출
        String authHeader = accessor.getFirstNativeHeader(TOKEN_HEADER);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
}
