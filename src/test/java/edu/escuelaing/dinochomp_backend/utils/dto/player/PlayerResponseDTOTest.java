package edu.escuelaing.dinochomp_backend.utils.dto.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerResponseDTOTest {

    @Test
    void builder_setsAllFields() {
        PlayerResponseDTO dto = PlayerResponseDTO.builder()
                .id("p1")
                .name("Alice")
                .email("alice@example.com")
                .password("secret")
                .positionX(10)
                .positionY(20)
                .health(30)
                .isAlive(true)
                .build();
        assertEquals("p1", dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("secret", dto.getPassword());
        assertEquals(10, dto.getPositionX());
        assertEquals(20, dto.getPositionY());
        assertEquals(30, dto.getHealth());
        assertTrue(dto.isAlive());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        PlayerResponseDTO dto = new PlayerResponseDTO();
        dto.setId("p2");
        dto.setName("Bob");
        dto.setEmail("bob@example.com");
        dto.setPassword("pwd");
        dto.setPositionX(1);
        dto.setPositionY(2);
        dto.setHealth(3);
        dto.setAlive(false);
        assertEquals("p2", dto.getId());
        assertEquals("Bob", dto.getName());
        assertEquals("bob@example.com", dto.getEmail());
        assertEquals("pwd", dto.getPassword());
        assertEquals(1, dto.getPositionX());
        assertEquals(2, dto.getPositionY());
        assertEquals(3, dto.getHealth());
        assertFalse(dto.isAlive());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        PlayerResponseDTO dto = new PlayerResponseDTO("p3","Carol","carol@mail","pass",7,8,9,true);
        assertEquals("p3", dto.getId());
        assertEquals("Carol", dto.getName());
        assertEquals("carol@mail", dto.getEmail());
        assertEquals("pass", dto.getPassword());
        assertEquals(7, dto.getPositionX());
        assertEquals(8, dto.getPositionY());
        assertEquals(9, dto.getHealth());
        assertTrue(dto.isAlive());
    }
}

