package edu.escuelaing.dinochomp_backend.utils.dto.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerPositionDTOTest {

    @Test
    void builder_setsAllFields() {
        PlayerPositionDTO dto = PlayerPositionDTO.builder()
                .id("p1")
                .name("Alice")
                .positionX(4)
                .positionY(5)
                .health(6)
                .isAlive(true)
                .build();
        assertEquals("p1", dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals(4, dto.getPositionX());
        assertEquals(5, dto.getPositionY());
        assertEquals(6, dto.getHealth());
        assertTrue(dto.isAlive());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        PlayerPositionDTO dto = new PlayerPositionDTO();
        dto.setId("p2");
        dto.setName("Bob");
        dto.setPositionX(1);
        dto.setPositionY(2);
        dto.setHealth(3);
        dto.setAlive(false);
        assertEquals("p2", dto.getId());
        assertEquals("Bob", dto.getName());
        assertEquals(1, dto.getPositionX());
        assertEquals(2, dto.getPositionY());
        assertEquals(3, dto.getHealth());
        assertFalse(dto.isAlive());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        PlayerPositionDTO dto = new PlayerPositionDTO("p3","Carol",7,8,9,true);
        assertEquals("p3", dto.getId());
        assertEquals("Carol", dto.getName());
        assertEquals(7, dto.getPositionX());
        assertEquals(8, dto.getPositionY());
        assertEquals(9, dto.getHealth());
        assertTrue(dto.isAlive());
    }
}

