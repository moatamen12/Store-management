package univ.StockManger.StockManger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // The endpoint clients will connect to, with SockJS fallback
        registry.addEndpoint("/ws").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // The prefix for messages to be routed to the broker (for broadcasting)
        registry.enableSimpleBroker("/topic", "/user");
        // The prefix for messages bound for @MessageMapping-annotated methods
        registry.setApplicationDestinationPrefixes("/app");
        // The prefix for user-specific destinations
        registry.setUserDestinationPrefix("/user");
    }
}
