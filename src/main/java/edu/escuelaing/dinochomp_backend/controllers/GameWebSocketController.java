package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerActivationtDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class GameWebSocketController {

    private final GameService gameService;
    private final SimpMessagingTemplate template;

    private static final String TOPIC = "/topic/games/";

    @MessageMapping("/games/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        gameService.startGameLoop(gameId);
        template.convertAndSend(TOPIC + gameId + "/status", "Game started!");
    }

    @MessageMapping("/games/{gameId}/stop")
    public void stopGame(@DestinationVariable String gameId) {
        gameService.stopGameLoop(gameId);
        template.convertAndSend(TOPIC + gameId + "/status", "Game stopped!");
    }

    @MessageMapping("/games/{gameId}/power/claim")
    public void claimPower(@DestinationVariable String gameId, @Payload PowerActivationtDTO msg) {
        gameService.claimPower(msg.getGameId(), msg.getPlayerId());
    }

    @MessageMapping("/games/{gameId}/power/use")
    public void usePower(@DestinationVariable String gameId, String playerId) {
        gameService.usePower(gameId, playerId);
    }

    @MessageMapping("/games/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload PlayerMoveMessage msg) {
        if (msg == null || msg.getPlayerId() == null || msg.getDirection() == null) {
            return;
        }
        Player updated = gameService.movePlayer(gameId, msg.getPlayerId(), msg.getDirection());
        if (updated == null) {
            log.info("Movimiento inválido o jugador no encontrado");
            return; // Evita el NPE
        }
        PlayerPositionDTO dto = PlayerPositionDTO.builder()
                .id(updated.getId())
                .name(updated.getName())
                .positionX(updated.getPositionX())
                .positionY(updated.getPositionY())
                .health(updated.getHealth())
                .isAlive(updated.isAlive())
                .build();

        template.convertAndSend(TOPIC + gameId + "/players", dto);
    }

    @MessageMapping("/games/{gameId}/join")
    public void joinGame(@DestinationVariable String gameId, @Payload Player player) {
        if (player == null || player.getId() == null) {
        log.info("Jugador inválido en join");
        return;
        }
        gameService.registerPlayer(gameId, player);
    }

    
}