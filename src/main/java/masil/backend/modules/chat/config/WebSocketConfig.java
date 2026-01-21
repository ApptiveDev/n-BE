package masil.backend.modules.chat.config;

import lombok.RequiredArgsConstructor;
import masil.backend.modules.chat.interceptor.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트가 구독할 수 있는 destination prefix
        config.enableSimpleBroker("/queue", "/topic");
        
        // 클라이언트가 메시지를 보낼 때 사용하는 destination prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // 특정 사용자에게 메시지를 보낼 때 사용하는 prefix
        config.setUserDestinationPrefix("/user");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 연결 엔드포인트
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*") // CORS 설정 (프로덕션에서는 특정 도메인으로 제한)
                .addInterceptors(webSocketAuthInterceptor)
                .withSockJS(); // SockJS 지원 (폴백 옵션)
    }
}
