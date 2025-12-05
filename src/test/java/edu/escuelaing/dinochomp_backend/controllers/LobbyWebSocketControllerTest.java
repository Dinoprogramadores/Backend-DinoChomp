package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.LobbyService;
import edu.escuelaing.dinochomp_backend.services.RedisPubSubService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LobbyWebSocketControllerTest {

    @Mock
    private LobbyService lobbyService;
    @Mock
    private RedisPubSubService redisPubSubService;

    private LobbyWebSocketController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        controller = new LobbyWebSocketController(lobbyService, redisPubSubService);
    }

    private String encrypt(Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        AesCrypto.Encrypted encrypted = AesCrypto.encrypt(json);
        // Simula el formato de payload que el cliente enviaría
        return mapper.writeValueAsString(Map.of("iv", encrypted.iv(), "ciphertext", encrypted.ciphertext()));
    }

    @Test
    void joinLobby_validPlayer_addsAndPublishes() throws Exception {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");
        p1.setName("Alice");

        when(lobbyService.getPlayers(gameId)).thenReturn(List.of(p1));

        controller.joinLobby(gameId, encrypt(p1));

        verify(lobbyService).addPlayer(eq(gameId), any(Player.class));
        verify(redisPubSubService).publishLobbyEvent(eq(gameId), eq("players"), any());
    }

    @Test
    void leaveLobby_removesAndPublishes() throws Exception {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");

        when(lobbyService.getPlayers(gameId)).thenReturn(List.of());

        controller.leaveLobby(gameId, encrypt(p1));

        verify(lobbyService).removePlayer(gameId, "p1");
        verify(redisPubSubService).publishLobbyEvent(eq(gameId), eq("players"), any());
    }

    @Test
    void startGame_publishesStartMessage() throws Exception {
        String gameId = "g1";
        controller.startGame(gameId, encrypt(""));
        verify(redisPubSubService).publishLobbyEvent(eq(gameId), eq("start"), any());
    }
}
