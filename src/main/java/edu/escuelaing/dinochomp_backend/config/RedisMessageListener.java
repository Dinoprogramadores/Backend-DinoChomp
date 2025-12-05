package edu.escuelaing.dinochomp_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class RedisMessageListener implements MessageListener {

    @Autowired
    private SimpMessagingTemplate template;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Map<String, String> PLURAL_MAP = Map.of(
            "lobby", "lobbies",
            "game", "games"
    );

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String payload = new String(message.getBody());

            log.info("Mensaje recibido de Redis en canal: {}", channel);
            log.info("Payload recibido: {}", payload);

            String[] parts = channel.split(":");
            if (parts.length >= 3) {
                String type = parts[0];  // "game" o "lobby"
                String gameId = parts[1];
                String channelType = parts[2];

                String pluralType = PLURAL_MAP.getOrDefault(type, type + "s");
                String wsTopic = "/topic/" + pluralType + "/" + gameId + "/" + channelType;

                Object messageObject = objectMapper.readValue(payload, Object.class);
                log.info("Objeto parseado: {}", messageObject);

                template.convertAndSend(wsTopic, messageObject);

                log.info("Mensaje reenviado a WebSocket: {}", wsTopic);
            }
        } catch (Exception e) {
            log.error("Error procesando mensaje de Redis", e);
        }
    }
}