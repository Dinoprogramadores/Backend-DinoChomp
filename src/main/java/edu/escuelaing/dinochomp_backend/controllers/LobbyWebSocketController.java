package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class LobbyWebSocketController {

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/lobbies/{gameId}/join")
    public void joinLobby(@DestinationVariable String gameId, @Payload Player player) {
        System.out.println("📥 JOIN recibido en lobby " + gameId + " -> " + player.getName());

        if (player == null || player.getId() == null) {
            System.err.println("❌ Jugador inválido en lobby join");
            return;
        }
        lobbyService.addPlayer(gameId, player);

        System.out.println("📤 Enviando lista de jugadores actualizada: " + lobbyService.getPlayers(gameId));
        template.convertAndSend("/topic/lobbies/" + gameId + "/players",
                lobbyService.getPlayers(gameId));
    }


    // Cliente envía a: /app/lobbies/{gameId}/leave
    @MessageMapping("/lobbies/{gameId}/leave")
    public void leaveLobby(@DestinationVariable String gameId, @Payload Player player) {
        lobbyService.removePlayer(gameId, player.getId());
        template.convertAndSend("/topic/lobbies/" + gameId + "/players",
                lobbyService.getPlayers(gameId));
    }

    // Cliente envía a: /app/lobbies/{gameId}/start
    @MessageMapping("/lobbies/{gameId}/start")
    public void startGame(@DestinationVariable String gameId) {
        // Notificar a todos los clientes que el juego debe comenzar
        template.convertAndSend("/topic/lobbies/" + gameId + "/start", "Game starting...");
    }
}
