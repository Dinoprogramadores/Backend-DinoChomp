package edu.escuelaing.dinochomp_backend.utils.dto.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddPlayerDTOTest {

    @Test
    void builder_setsAllFields() {
        AddPlayerDTO dto = AddPlayerDTO.builder()
                .playerId("p1")
                .positionX(1)
                .positionY(2)
                .build();
        assertEquals("p1", dto.getPlayerId());
        assertEquals(1, dto.getPositionX());
        assertEquals(2, dto.getPositionY());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        AddPlayerDTO dto = new AddPlayerDTO();
        dto.setPlayerId("p2");
        dto.setPositionX(3);
        dto.setPositionY(4);
        assertEquals("p2", dto.getPlayerId());
        assertEquals(3, dto.getPositionX());
        assertEquals(4, dto.getPositionY());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        AddPlayerDTO dto = new AddPlayerDTO("p3",7,8);
        assertEquals("p3", dto.getPlayerId());
        assertEquals(7, dto.getPositionX());
        assertEquals(8, dto.getPositionY());
    }
}

