package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import edu.escuelaing.dinochomp_backend.services.RedisPubSubService;
import edu.escuelaing.dinochomp_backend.utils.dto.encrypted.EncryptedPayload;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Slf4j
public class LobbyWebSocketController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private RedisPubSubService redisPubSubService;

    private final ObjectMapper mapper = new ObjectMapper();

    // ===============================
    // JOIN
    // ===============================
    @MessageMapping("/lobbies/{gameId}/join")
    public void joinLobby(@DestinationVariable String gameId, @Payload String payload) {
        try {
            EncryptedPayload encrypted = parsePayload(payload);
            if (encrypted == null) return;

            String json = AesCrypto.decrypt(encrypted.iv, encrypted.ciphertext);
            Player player = mapper.readValue(json, Player.class);

            if (player == null || player.getId() == null) return;

            log.info("🔐 JOIN recibido en lobby {} -> {}", gameId, player.getName());
            
            lobbyService.addPlayer(gameId, player);
            publishEncryptedLobbyEvent(gameId, "players", lobbyService.getPlayers(gameId));

        } catch (Exception e) {
            log.error("❌ Error procesando joinLobby", e);
        }
    }

    // ===============================
    // LEAVE
    // ===============================
    @MessageMapping("/lobbies/{gameId}/leave")
    public void leaveLobby(@DestinationVariable String gameId, @Payload String payload) {
        try {
            EncryptedPayload encrypted = parsePayload(payload);
            if (encrypted == null) return;

            String json = AesCrypto.decrypt(encrypted.iv, encrypted.ciphertext);
            Player player = mapper.readValue(json, Player.class);

            if (player == null || player.getId() == null) return;

            log.info("🔐 LEAVE recibido en lobby {} -> {}", gameId, player.getName());
            
            lobbyService.removePlayer(gameId, player.getId());
            publishEncryptedLobbyEvent(gameId, "players", lobbyService.getPlayers(gameId));

        } catch (Exception e) {
            log.error("❌ Error procesando leaveLobby", e);
        }
    }

    // ===============================
    // START
    // ===============================
    @MessageMapping("/lobbies/{gameId}/start")
    public void startGame(@DestinationVariable String gameId, @Payload String payload) {
        try {
            EncryptedPayload encrypted = parsePayload(payload);
            if (encrypted == null) return;

            String json = AesCrypto.decrypt(encrypted.iv, encrypted.ciphertext);
            
            log.info("🚀 START game {}", gameId);
            publishEncryptedLobbyEvent(gameId, "start", Map.of("message", "Game starting..."));

        } catch (Exception e) {
            log.error("❌ Error procesando startLobby", e);
        }
    }

    // ===============================
    // Métodos privados
    // ===============================
    
    /**
     * Parsea el payload recibido del cliente, manejando comillas escapadas
     */
    private EncryptedPayload parsePayload(String rawPayload) {
        try {
            String cleanJson = rawPayload;

            // Si el cliente envía el JSON envuelto en comillas, las quitamos
            if (rawPayload.startsWith("\"") && rawPayload.endsWith("\"")) {
                cleanJson = rawPayload.substring(1, rawPayload.length() - 1)
                    .replace("\\\"", "\"");
            }

            return mapper.readValue(cleanJson, EncryptedPayload.class);
        } catch (Exception e) {
            log.error("❌ Error parseando payload: {}", rawPayload, e);
            return null;
        }
    }

    /**
     * 🔐 Cifra y publica eventos del lobby
     * Este método garantiza que TODOS los mensajes salgan cifrados
     */
    private void publishEncryptedLobbyEvent(String gameId, String channel, Object data) {
        try {
            // 1. Serializar data a JSON
            String json = mapper.writeValueAsString(data);
            log.debug("📝 Datos a cifrar: {}", json);
            
            // 2. Cifrar el JSON
            AesCrypto.Encrypted encrypted = AesCrypto.encrypt(json);

            // 3. Crear Map con iv y ciphertext
            Map<String, String> payload = Map.of(
                "iv", encrypted.iv(),
                "ciphertext", encrypted.ciphertext()
            );

            // 4. Publicar en Redis
            redisPubSubService.publishLobbyEvent(gameId, channel, payload);
            
            log.debug("🔐 Evento cifrado publicado: lobby:{}:{}", gameId, channel);

        } catch (Exception e) {
            log.error("❌ Error cifrando evento en lobby {} canal {}", gameId, channel, e);
        }
    }

}