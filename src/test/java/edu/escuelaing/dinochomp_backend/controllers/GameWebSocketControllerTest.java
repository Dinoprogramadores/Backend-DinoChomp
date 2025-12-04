package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerMoveMessage;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerActivationtDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GameWebSocketControllerTest {

    private GameService gameService;
    private SimpMessagingTemplate template;
    private GameWebSocketController controller;

    private static final String TOPIC = "/topic/games/";

    @BeforeEach
    void setUp() {
        gameService = mock(GameService.class);
        template = mock(SimpMessagingTemplate.class);
        controller = new GameWebSocketController(gameService, template);
    }

    @Test
    void startGame_sendsStatusAndStartsLoop() {
        String gameId = "g1";
        controller.startGame(gameId);
        verify(gameService).startGameLoop(gameId);
        verify(template).convertAndSend(TOPIC + gameId + "/status", "Game started!");
    }

    @Test
    void stopGame_sendsStatusAndStopsLoop() {
        String gameId = "g1";
        controller.stopGame(gameId);
        verify(gameService).stopGameLoop(gameId);
        verify(template).convertAndSend(TOPIC + gameId + "/status", "Game stopped!");
    }

    @Test
    void claimPower_invokesService() {
        PowerActivationtDTO dto = new PowerActivationtDTO();
        dto.setGameId("g1");
        dto.setPlayerId("p1");
        controller.claimPower("g1", dto);
        verify(gameService).claimPower("g1", "p1");
    }

    @Test
    void usePower_invokesService() {
        controller.usePower("g1", "p1");
        verify(gameService).usePower("g1", "p1");
    }

    @Test
    void handleMove_invalidMessage_isIgnored() {
        controller.handleMove("g1", null);
        controller.handleMove("g1", new PlayerMoveMessage());
        PlayerMoveMessage missingDir = new PlayerMoveMessage();
        missingDir.setPlayerId("p1");
        controller.handleMove("g1", missingDir);
        verifyNoInteractions(template);
        verify(gameService, never()).movePlayer(anyString(), anyString(), any());
    }

    @Test
    void handleMove_validMessage_sendsUpdatedPosition() {
        String gameId = "g1";
        PlayerMoveMessage msg = new PlayerMoveMessage();
        msg.setPlayerId("p1");
        msg.setDirection("UP");

        Player updated = new Player();
        updated.setId("p1");
        updated.setName("Alice");
        updated.setPositionX(3);
        updated.setPositionY(4);
        updated.setHealth(10);
        updated.setAlive(true);

        when(gameService.movePlayer(gameId, "p1", "UP")).thenReturn(updated);

        controller.handleMove(gameId, msg);

        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<String> destinationCaptor = ArgumentCaptor.forClass(String.class);
        verify(template).convertAndSend(destinationCaptor.capture(), payloadCaptor.capture());

        assertEquals(TOPIC + gameId + "/players", destinationCaptor.getValue());
        Object payload = payloadCaptor.getValue();
        assertInstanceOf(PlayerPositionDTO.class, payload);
        PlayerPositionDTO dto = (PlayerPositionDTO) payload;
        assertEquals("p1", dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals(3, dto.getPositionX());
        assertEquals(4, dto.getPositionY());
        assertEquals(10, dto.getHealth());
        assertTrue(dto.isAlive());
    }

    @Test
    void handleMove_serviceReturnsNull_doesNotSend() {
        String gameId = "g1";
        PlayerMoveMessage msg = new PlayerMoveMessage();
        msg.setPlayerId("p1");
        msg.setDirection("LEFT");

        when(gameService.movePlayer(anyString(), anyString(), any())).thenReturn(null);

        controller.handleMove(gameId, msg);
        verifyNoInteractions(template);
    }

    @Test
    void joinGame_invalidPlayer_isIgnored() {
        controller.joinGame("g1", null);
        Player invalid = new Player();
        controller.joinGame("g1", invalid);
        verify(gameService, never()).registerPlayer(anyString(), any());
    }

    @Test
    void joinGame_validPlayer_registers() {
        Player p = new Player();
        p.setId("p1");
        controller.joinGame("g1", p);
        verify(gameService).registerPlayer("g1", p);
    }
}
