package edu.escuelaing.dinochomp_backend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RedisPubSubService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public void publishEvent(String type, String gameId, String channel, Object message) {
        String redisChannel = type + ":" + gameId + ":" + channel;
        try {
            // Convertir el objeto a JSON string
            String jsonMessage = objectMapper.writeValueAsString(message);

            // 🔥 SOLO publicar en Redis
            // El RedisMessageListener se encargará de reenviarlo al WebSocket
            redisTemplate.convertAndSend(redisChannel, jsonMessage);

            log.info("✅ Publicado en Redis: {} - Payload: {}", redisChannel, jsonMessage);
        } catch (Exception e) {
            log.error("❌ Error publicando en Redis", e);
        }
    }

    public void publishGameEvent(String gameId, String channel, Object message) {
        publishEvent("game", gameId, channel, message);
    }

    public void publishLobbyEvent(String gameId, String channel, Object message) {
        publishEvent("lobby", gameId, channel, message);
    }
}