package edu.escuelaing.dinochomp_backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    @Lazy
    private RedisMessageListener redisMessageListener;

    @Autowired
    private RedisMessageListenerContainer redisMessageListenerContainer;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/ws/lobbies")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(true)
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);

        registry.addEndpoint("/ws/games")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setSessionCookieNeeded(true)
                .setStreamBytesLimit(512 * 1024)
                .setHttpMessageCacheSize(1000)
                .setDisconnectDelay(30 * 1000);

        // Redis listeners
        redisMessageListenerContainer.addMessageListener(
                redisMessageListener,
                new PatternTopic("game:*")
        );
        redisMessageListenerContainer.addMessageListener(
                redisMessageListener,
                new PatternTopic("lobby:*")
        );
    }
}