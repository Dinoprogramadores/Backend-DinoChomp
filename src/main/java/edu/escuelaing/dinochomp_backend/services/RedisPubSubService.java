package edu.escuelaing.dinochomp_backend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisPubSubService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate template;

    public void publishEvent(String type, String gameId, String channel, Object message) {
        String redisChannel = type + ":" + gameId + ":" + channel;
        redisTemplate.convertAndSend(redisChannel, message);
        log.info("Evento publicado en Redis: {}", redisChannel);
    }

    public void publishGameEvent(String gameId, String channel, Object message) {
        publishEvent("game", gameId, channel, message);
    }

    public void publishLobbyEvent(String gameId, String channel, Object message) {
        publishEvent("lobby", gameId, channel, message);
    }

}