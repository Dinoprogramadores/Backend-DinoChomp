package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.AesCrypto;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.services.RedisPubSubService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerActivationtDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameWebSocketControllerTest {

    @Mock
    private GameService gameService;
    @Mock
    private RedisPubSubService redisPubSubService;

    private GameWebSocketController controller;
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        controller = new GameWebSocketController(gameService, redisPubSubService);
    }

    private Map<String, String> encrypt(Object obj) throws Exception {
        String json = mapper.writeValueAsString(obj);
        AesCrypto.Encrypted encrypted = AesCrypto.encrypt(json);
        return Map.of("iv", encrypted.iv(), "ciphertext", encrypted.ciphertext());
    }

    @Test
    void startGame_sendsStatusAndStartsLoop() throws Exception {
        String gameId = "g1";
        controller.startGame(gameId, encrypt(""));
        verify(gameService).startGameLoop(gameId);
        verify(redisPubSubService).publishGameEvent(eq(gameId), eq("status"), any());
    }

    @Test
    void stopGame_sendsStatusAndStopsLoop() throws Exception {
        String gameId = "g1";
        controller.stopGame(gameId, encrypt(""));
        verify(gameService).stopGameLoop(gameId);
        verify(redisPubSubService).publishGameEvent(eq(gameId), eq("status"), any());
    }

    @Test
    void claimPower_invokesService() throws Exception {
        String gameId = "g1";
        String playerId = "p1";
        PowerActivationtDTO dto = new PowerActivationtDTO(gameId, playerId);
        controller.claimPower(gameId, encrypt(dto));
        verify(gameService).claimPower(gameId, playerId);
    }

    @Test
    void usePower_invokesService() throws Exception {
        String gameId = "g1";
        String playerId = "p1";
        PowerActivationtDTO dto = new PowerActivationtDTO(gameId, playerId);
        controller.usePower(gameId, encrypt(dto));
        verify(gameService).usePower(gameId, playerId);
    }

    @Test
    void handleMove_validMessage_sendsUpdatedPosition() throws Exception {
        String gameId = "g1";
        PlayerMoveMessage msg = new PlayerMoveMessage("p1", "UP");

        Player updated = new Player();
        updated.setId("p1");
        when(gameService.movePlayer(gameId, "p1", "UP")).thenReturn(updated);

        controller.handleMove(gameId, encrypt(msg));

        verify(gameService).movePlayer(gameId, "p1", "UP");
        verify(redisPubSubService).publishGameEvent(eq(gameId), eq("players"), any());
    }

    @Test
    void handleMove_serviceReturnsNull_doesNotSend() throws Exception {
        String gameId = "g1";
        PlayerMoveMessage msg = new PlayerMoveMessage("p1", "LEFT");
        when(gameService.movePlayer(anyString(), anyString(), anyString())).thenReturn(null);

        controller.handleMove(gameId, encrypt(msg));

        verify(redisPubSubService, never()).publishGameEvent(any(), any(), any());
    }

    @Test
    void joinGame_validPlayer_registers() throws Exception {
        String gameId = "g1";
        Player p = new Player();
        p.setId("p1");

        controller.joinGame(gameId, encrypt(p));

        verify(gameService).registerPlayer(eq(gameId), any(Player.class));
        verify(redisPubSubService).publishGameEvent(eq(gameId), eq("player-joined"), any());
    }
}
