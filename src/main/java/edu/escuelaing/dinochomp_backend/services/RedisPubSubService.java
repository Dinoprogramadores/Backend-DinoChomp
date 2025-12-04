package edu.escuelaing.dinochomp_backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class RedisPubSubService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate template;

    public void publishGameEvent(String gameId, String channel, Object message) {
        String redisChannel = "game:" + gameId + ":" + channel;
        redisTemplate.convertAndSend(redisChannel, message);
        log.info("Evento publicado en Redis: {}", redisChannel);
    }

    public void publishToWebSocket(String topic, Object message) {
        template.convertAndSend(topic, message);
    }
}