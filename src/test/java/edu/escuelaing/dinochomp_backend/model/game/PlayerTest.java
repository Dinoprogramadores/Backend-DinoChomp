package edu.escuelaing.dinochomp_backend.model.game;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testConstructorAndGetters() {
        Player player = Player.builder()
                .id("p1")
                .name("Santiago")
                .email("san@correo.com")
                .password("123")
                .positionX(10)
                .positionY(20)
                .health(100)
                .isAlive(true)
                .build();

        assertEquals("p1", player.getId());
        assertEquals("Santiago", player.getName());
        assertEquals("san@correo.com", player.getEmail());
        assertEquals("123", player.getPassword());
        assertEquals(10, player.getPositionX());
        assertEquals(20, player.getPositionY());
        assertEquals(100, player.getHealth());
        assertTrue(player.isAlive());
    }

    @Test
    void testSetters() {
        Player player = new Player();

        player.setId("p2");
        player.setName("Carlos");
        player.setEmail("carlos@mail.com");
        player.setPassword("abc");
        player.setPositionX(5);
        player.setPositionY(7);
        player.setHealth(75);
        player.setAlive(false);

        assertEquals("p2", player.getId());
        assertEquals("Carlos", player.getName());
        assertEquals("carlos@mail.com", player.getEmail());
        assertEquals("abc", player.getPassword());
        assertEquals(5, player.getPositionX());
        assertEquals(7, player.getPositionY());
        assertEquals(75, player.getHealth());
        assertFalse(player.isAlive());
    }
}
