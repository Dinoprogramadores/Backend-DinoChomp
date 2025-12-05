package edu.escuelaing.dinochomp_backend.utils.dto.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovePlayerDTOTest {

    @Test
    void builder_setsAllFields() {
        MovePlayerDTO dto = MovePlayerDTO.builder()
                .playerId("p1")
                .newX(5)
                .newY(6)
                .build();
        assertEquals("p1", dto.getPlayerId());
        assertEquals(5, dto.getNewX());
        assertEquals(6, dto.getNewY());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        MovePlayerDTO dto = new MovePlayerDTO();
        dto.setPlayerId("p2");
        dto.setNewX(7);
        dto.setNewY(8);
        assertEquals("p2", dto.getPlayerId());
        assertEquals(7, dto.getNewX());
        assertEquals(8, dto.getNewY());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        MovePlayerDTO dto = new MovePlayerDTO("p3",9,10);
        assertEquals("p3", dto.getPlayerId());
        assertEquals(9, dto.getNewX());
        assertEquals(10, dto.getNewY());
    }
}

