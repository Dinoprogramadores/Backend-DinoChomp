package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerMapperTest {

    private final PlayerMapper mapper = new PlayerMapper();

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_buildsPlayer() {
        PlayerRequestDTO dto = PlayerRequestDTO.builder()
                .id("p1")
                .name("Alice")
                .email("alice@example.com")
                .password("secret")
                .positionX(5)
                .positionY(6)
                .health(10)
                .isAlive(true)
                .build();
        Player p = mapper.toEntity(dto);
        assertEquals("p1", p.getId());
        assertEquals("Alice", p.getName());
        assertEquals("alice@example.com", p.getEmail());
        assertEquals("secret", p.getPassword());
        assertEquals(5, p.getPositionX());
        assertEquals(6, p.getPositionY());
        assertEquals(10, p.getHealth());
        assertTrue(p.isAlive());
    }

    @Test
    void toDTO_null_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_buildsResponse() {
        Player p = Player.builder()
                .id("p1")
                .name("Alice")
                .email("alice@example.com")
                .password("secret")
                .positionX(1)
                .positionY(2)
                .health(7)
                .isAlive(false)
                .build();
        PlayerResponseDTO dto = mapper.toDTO(p);
        assertEquals("p1", dto.getId());
        assertEquals("Alice", dto.getName());
        assertEquals("alice@example.com", dto.getEmail());
        assertEquals("secret", dto.getPassword());
        assertEquals(1, dto.getPositionX());
        assertEquals(2, dto.getPositionY());
        assertEquals(7, dto.getHealth());
        assertFalse(dto.isAlive());
    }
}

