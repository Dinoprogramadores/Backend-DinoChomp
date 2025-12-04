package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LobbyWebSocketControllerTest {

    private LobbyService lobbyService;
    private SimpMessagingTemplate template;
    private LobbyWebSocketController controller;

    private static final String TOPIC = "/topic/lobbies/";

    @BeforeEach
    void setUp() {
        lobbyService = mock(LobbyService.class);
        template = mock(SimpMessagingTemplate.class);
        controller = new LobbyWebSocketController(lobbyService, template);
    }

    @Test
    void joinLobby_invalidPlayer_isIgnored() {
        controller.joinLobby("g1", new Player());
        verify(lobbyService, never()).addPlayer(anyString(), any());
        verifyNoInteractions(template);
    }

    @Test
    void joinLobby_validPlayer_addsAndSendsList() {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");
        p1.setName("Alice");

        List<Player> players = List.of(p1);
        when(lobbyService.getPlayers(gameId)).thenReturn(players);

        controller.joinLobby(gameId, p1);

        verify(lobbyService).addPlayer(gameId, p1);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        verify(template).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertEquals(TOPIC + gameId + "/players", destinationCaptor.getValue());
        assertEquals(players, payloadCaptor.getValue());
    }

    @Test
    void leaveLobby_removesAndSendsList() {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");
        List<Player> remaining = List.of();
        when(lobbyService.getPlayers(gameId)).thenReturn(remaining);

        controller.leaveLobby(gameId, p1);

        verify(lobbyService).removePlayer(gameId, "p1");
        verify(template).convertAndSend(TOPIC + gameId + "/players", remaining);
    }

    @Test
    void startGame_sendsStartMessage() {
        String gameId = "g1";
        controller.startGame(gameId);
        verify(template).convertAndSend(TOPIC + gameId + "/start", "Game starting...");
        verifyNoInteractions(lobbyService);
    }
}
