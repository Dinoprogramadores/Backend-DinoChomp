package edu.escuelaing.dinochomp_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // tópico para broadcast
        config.setApplicationDestinationPrefixes("/app"); // destino que maneja @MessageMapping
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // URL donde los clientes se conectan
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
