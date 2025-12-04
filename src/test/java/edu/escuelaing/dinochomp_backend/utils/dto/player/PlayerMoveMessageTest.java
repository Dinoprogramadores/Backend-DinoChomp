package edu.escuelaing.dinochomp_backend.utils.dto.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMoveMessageTest {

    @Test
    void builder_setsAllFields() {
        PlayerMoveMessage dto = PlayerMoveMessage.builder()
                .playerId("p1")
                .direction("UP")
                .build();
        assertEquals("p1", dto.getPlayerId());
        assertEquals("UP", dto.getDirection());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        PlayerMoveMessage dto = new PlayerMoveMessage();
        dto.setPlayerId("p2");
        dto.setDirection("LEFT");
        assertEquals("p2", dto.getPlayerId());
        assertEquals("LEFT", dto.getDirection());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        PlayerMoveMessage dto = new PlayerMoveMessage("p3","DOWN");
        assertEquals("p3", dto.getPlayerId());
        assertEquals("DOWN", dto.getDirection());
    }
}

