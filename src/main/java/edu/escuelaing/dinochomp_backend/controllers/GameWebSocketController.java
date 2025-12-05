package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerActivationtDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@Slf4j
public class GameWebSocketController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate template;

    private static final String TOPIC = "/topic/games/";
    private final ObjectMapper mapper = new ObjectMapper();

    @MessageMapping("/games/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        gameService.startGameLoop(gameId);
        template.convertAndSend(TOPIC + gameId + "/status", encryptResponse("Game started!"));
    }

    @MessageMapping("/games/{gameId}/stop")
    public void stopGame(@DestinationVariable String gameId) {
        gameService.stopGameLoop(gameId);
         template.convertAndSend(TOPIC + gameId + "/status", encryptResponse("Game stopped!"));
    }

    @MessageMapping("/games/{gameId}/power/claim")
    public void claimPower(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) {

        Map<String, Object> data = decryptPayload(encrypted);

        String gId = data.get("gameId").toString();
        String pId = data.get("playerId").toString();

        gameService.claimPower(gId, pId);
    }

    @MessageMapping("/games/{gameId}/power/use")
    public void usePower(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) {

        Map<String, Object> data = decryptPayload(encrypted);

        String pId = data.get("playerId").toString();
        gameService.usePower(gameId, pId);
    }

     @MessageMapping("/games/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) {

        Map<String, Object> data = decryptPayload(encrypted);

        String playerId = data.get("playerId").toString();
        String direction = data.get("direction").toString();

        Player updated = gameService.movePlayer(gameId, playerId, direction);
        if (updated == null) return;

        PlayerPositionDTO dto = PlayerPositionDTO.builder()
                .id(updated.getId())
                .name(updated.getName())
                .positionX(updated.getPositionX())
                .positionY(updated.getPositionY())
                .health(updated.getHealth())
                .isAlive(updated.isAlive())
                .build();

        template.convertAndSend(
                TOPIC + gameId + "/players",
                encryptResponse(dto)     // 🔐 envío cifrado
        );
    }

    @MessageMapping("/games/{gameId}/join")
    public void joinGame(@DestinationVariable String gameId, @Payload Map<String, String> encrypted) {

        Map<String, Object> data = decryptPayload(encrypted);

        Player player = new Player();
        player.setId(data.get("id").toString());
        player.setName(data.get("name").toString());
        player.setEmail(data.get("email").toString());
        player.setPositionX((int) data.get("positionX"));
        player.setPositionY((int) data.get("positionY"));
        player.setAlive((boolean) data.get("alive"));
        player.setHealth((int) data.get("health"));

        gameService.registerPlayer(gameId, player);
    }

    // -------------------------------------------------------
    // 🔓 UTILERÍA PARA DESCIFRAR MENSAJE ENTRANTE
    // -------------------------------------------------------
    private Map<String, Object> decryptPayload(Map<String, String> encrypted) {
        try {
            String json = AesCrypto.decrypt(
                    encrypted.get("iv"),
                    encrypted.get("ciphertext")
            );
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("❌ Error descifrando payload", e);
            throw new RuntimeException("Invalid encrypted payload");
        }
    }

    // -------------------------------------------------------
    // 🔐 UTILERÍA PARA CIFRAR RESPUESTAS AL FRONTEND
    // -------------------------------------------------------
    private Map<String, String> encryptResponse(Object data) {
        try {
            String json = mapper.writeValueAsString(data);
            AesCrypto.Encrypted enc = AesCrypto.encrypt(json);

            return Map.of(
                    "iv", enc.iv(),
                    "ciphertext", enc.ciphertext()
            );
        } catch (Exception e) {
            log.error("❌ Error cifrando respuesta", e);
            throw new RuntimeException("Encryption error");
        }
    }

    
}