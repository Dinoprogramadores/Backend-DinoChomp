package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class GameWebSocketController {

    @Autowired
    private GameService gameService;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/games/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        gameService.startGameLoop(gameId);
        template.convertAndSend("/topic/games/" + gameId + "/status", "Game started!");
    }

    @MessageMapping("/games/{gameId}/stop")
    public void stopGame(@DestinationVariable String gameId) {
        gameService.stopGameLoop(gameId);
        template.convertAndSend("/topic/games/" + gameId + "/status", "Game stopped!");
    }

    @MessageMapping("/games/{gameId}/power/claim")
    public void claimPower(@DestinationVariable String gameId, String playerId) {
        gameService.claimPower(gameId, playerId);
    }

    @MessageMapping("/games/{gameId}/power/use")
    public void usePower(@DestinationVariable String gameId, String playerId) {
        gameService.usePower(gameId, playerId);
    }
    // Cliente envía a: /app/games/{gameId}/move
    @MessageMapping("/games/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload PlayerMoveMessage msg) {
        if (msg == null || msg.getPlayerId() == null || msg.getDirection() == null) {
            return;
        }
        Player updated = gameService.movePlayer(gameId, msg.getPlayerId(), msg.getDirection());
        if (updated == null) {
            System.out.println("Movimiento inválido o jugador no encontrado"
                    );
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

        template.convertAndSend("/topic/games/" + gameId + "/players", dto);
    }

    @MessageMapping("/games/{gameId}/join")
    public void joinGame(@DestinationVariable String gameId, @Payload Player player) {
        if (player == null || player.getId() == null) {
        System.err.println("Jugador inválido en join");
        return;
        }
        gameService.registerPlayer(gameId, player);
    }
}