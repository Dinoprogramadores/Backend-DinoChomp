package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.PlayerPositionDTO;
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

    // Cliente envía a: /app/games/{gameId}/move
    @MessageMapping("/games/{gameId}/move")
    public void handleMove(@DestinationVariable String gameId, @Payload PlayerMoveMessage msg) {
        if (msg == null || msg.getPlayerId() == null || msg.getDirection() == null) {
            return;
        }

        // se asume que gameService.movePlayer aplica validaciones y retorna el Player actualizado o null
        Player p = gameService.movePlayer(msg.getPlayerId(), msg.getDirection());
        if (p != null) {
            PlayerPositionDTO dto = new PlayerPositionDTO(
                    p.getId(),
                    p.getPositionX(),
                    p.getPositionY(),
                    p.getHealth(),
                    p.isAlive()
            );
            template.convertAndSend("/topic/games/" + gameId + "/players", dto);
        }
    }
}