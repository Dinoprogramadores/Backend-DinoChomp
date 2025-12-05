package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.services.RedisPubSubService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerActivationtDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GameWebSocketController {

    private final GameService gameService;
    private final RedisPubSubService redisPubSubService;

    private final ObjectMapper mapper = new ObjectMapper();

    @MessageMapping("/games/{gameId}/start")
    public void startGame(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {
        gameService.startGameLoop(gameId);
        publishEncryptedEvent(gameId, "status", "Game started!");
    }

    @MessageMapping("/games/{gameId}/stop")
    public void stopGame(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {
        gameService.stopGameLoop(gameId);
        publishEncryptedEvent(gameId, "status", "Game stopped!");
    }

    @MessageMapping("/games/{gameId}/power/claim")
    public void claimPower(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {

        try {
            String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));
            PowerActivationtDTO msg = mapper.readValue(json, PowerActivationtDTO.class);

            gameService.claimPower(msg.getGameId(), msg.getPlayerId());

        } catch (Exception e) {
            log.error("Error descifrando claimPower", e);
        }
    }

    @MessageMapping("/games/{gameId}/power/use")
    public void usePower(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {
        try {
            String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));
            PowerActivationtDTO msg = mapper.readValue(json, PowerActivationtDTO.class);

            gameService.usePower(gameId, msg.getPlayerId());
        } catch (Exception e) {
            log.error("Error descifrando usePower", e);
        }
    }

    @MessageMapping("/games/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {

        try {
            // Descifrar JSON
            String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));
            PlayerMoveMessage msg = mapper.readValue(json, PlayerMoveMessage.class);

            if (msg == null || msg.getPlayerId() == null || msg.getDirection() == null) {
                return;
            }

            // Procesar el movimiento
            Player updated = gameService.movePlayer(gameId, msg.getPlayerId(), msg.getDirection());
            if (updated == null) {
                log.info("Movimiento inválido o jugador no encontrado");
                return;
            }

            // Construir DTO
            PlayerPositionDTO dto = PlayerPositionDTO.builder()
                    .id(updated.getId())
                    .name(updated.getName())
                    .positionX(updated.getPositionX())
                    .positionY(updated.getPositionY())
                    .health(updated.getHealth())
                    .isAlive(updated.isAlive())
                    .build();

            // Publicar posición cifrada
            publishEncryptedEvent(gameId, "players", dto);

        } catch (Exception e) {
            log.error("Error procesando movimiento cifrado", e);
        }
    }

    @MessageMapping("/games/{gameId}/join")
    public void joinGame(@DestinationVariable String gameId,
            @Payload Map<String, String> encrypted) {

        try {
            String json = AesCrypto.decrypt(encrypted.get("iv"), encrypted.get("ciphertext"));
            Player player = mapper.readValue(json, Player.class);

            if (player == null || player.getId() == null) {
                log.info("Jugador inválido en join");
                return;
            }

            gameService.registerPlayer(gameId, player);

            // Notificar a otros jugadores (cifrado)
            publishEncryptedEvent(gameId, "player-joined", player);

        } catch (Exception e) {
            log.error("Error descifrando join", e);
        }
    }

    private void publishEncryptedEvent(String gameId, String channel, Object data) {
        try {
            // 1. Serializar data a JSON
            String json = mapper.writeValueAsString(data);

            // 2. Cifrar el JSON
            AesCrypto.Encrypted encrypted = AesCrypto.encrypt(json);

            // 3. ✅ Crear Map con iv y ciphertext (NO enviar el objeto Encrypted directamente)
            Map<String, String> payload = Map.of(
                "iv", encrypted.iv(),
                "ciphertext", encrypted.ciphertext()
            );

            // 4. Publicar el Map
            redisPubSubService.publishGameEvent(gameId, channel, payload);

        } catch (Exception e) {
            log.error("Error cifrando evento {}", channel, e);
        }
    }

}