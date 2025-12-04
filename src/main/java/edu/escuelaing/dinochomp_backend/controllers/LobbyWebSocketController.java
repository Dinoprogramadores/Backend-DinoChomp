package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import edu.escuelaing.dinochomp_backend.services.RedisPubSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class LobbyWebSocketController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private RedisPubSubService redisPubSubService;

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
        redisPubSubService.publishGameEvent(gameId, "players", lobbyService.getPlayers(gameId));
    }

    @MessageMapping("/lobbies/{gameId}/leave")
    public void leaveLobby(@DestinationVariable String gameId, @Payload Player player) {
        lobbyService.removePlayer(gameId, player.getId());
        redisPubSubService.publishGameEvent(gameId, "players", lobbyService.getPlayers(gameId));
    }

    @MessageMapping("/lobbies/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        redisPubSubService.publishGameEvent(gameId, "start", "Game starting...");
    }
}