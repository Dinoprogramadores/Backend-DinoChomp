package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
@RequiredArgsConstructor
public class LobbyWebSocketController {

    private final LobbyService lobbyService;
    private final SimpMessagingTemplate template;

    private static final String TOPIC = "/topic/lobbies/";

    @MessageMapping("/lobbies/{gameId}/join")
    public void joinLobby(@DestinationVariable String gameId, @Payload Player player) {
        log.info("JOIN recibido en lobby {} -> {}", gameId, player.getName());

        if (player.getId() == null) {
            log.warn("Jugador inválido en lobby join");
            return;
        }
        lobbyService.addPlayer(gameId, player);

        log.info("Enviando lista de jugadores actualizada: {}", lobbyService.getPlayers(gameId));
        template.convertAndSend(TOPIC + gameId + "/players",
                lobbyService.getPlayers(gameId));
    }


    @MessageMapping("/lobbies/{gameId}/leave")
    public void leaveLobby(@DestinationVariable String gameId, @Payload Player player) {
        lobbyService.removePlayer(gameId, player.getId());
        template.convertAndSend(TOPIC + gameId + "/players",
                lobbyService.getPlayers(gameId));
    }

    @MessageMapping("/lobbies/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        template.convertAndSend(TOPIC + gameId + "/start", "Game starting...");
    }
}
