package edu.escuelaing.dinochomp_backend.model.game;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    @Test
    void testAddPlayerDinosaur_WithinLimit() {
        Game game = Game.builder().build();

        boolean added1 = game.addPlayerDinosaur("p1", new Dinosaur());
        boolean added2 = game.addPlayerDinosaur("p2", new Dinosaur());
        boolean added3 = game.addPlayerDinosaur("p3", new Dinosaur());
        boolean added4 = game.addPlayerDinosaur("p4", new Dinosaur());

        assertTrue(added1);
        assertTrue(added2);
        assertTrue(added3);
        assertTrue(added4);
        assertEquals(4, game.getPlayerDinosaurMap().size());
    }

    @Test
    void testAddPlayerDinosaur_ExceedsLimit() {
        Game game = Game.builder().build();

        // 4 jugadores válidos
        for (int i = 1; i <= 4; i++) {
            assertTrue(game.addPlayerDinosaur("p" + i, new Dinosaur()));
        }

        // 5° debería fallar
        assertFalse(game.addPlayerDinosaur("p5", new Dinosaur()));
        assertEquals(4, game.getPlayerDinosaurMap().size());
    }

    @Test
    void testTimerRemainingSeconds() {
        Game game = Game.builder()
                .durationMinutes(1)  // 60 segundos
                .startTime(Instant.now().minusSeconds(20))
                .timerActive(true)
                .build();

        Long remaining = game.getRemainingSeconds();

        assertNotNull(remaining);
        assertTrue(remaining <= 60 && remaining >= 35);
    }

    @Test
    void testRefreshTimerState_StopsOnZero() {
        Game game = Game.builder()
                .durationMinutes(0)      // contador = 0
                .startTime(Instant.now().minusSeconds(5))
                .timerActive(true)
                .build();

        game.refreshTimerState();

        assertFalse(game.isTimerActive());
    }

    @Test
    void testDefaultValues() {
        Game game = new Game();

        assertNull(game.getNombre());
        assertEquals(0, game.getDurationMinutes());
        assertFalse(game.isTimerActive());
        assertNotNull(game.getPlayerDinosaurMap());
        assertNotNull(game.getPowers());
    }

    @Test
    void testBuilderFullObject() {
        Game game = Game.builder()
                .nombre("game1")
                .isActive(true)
                .width(10)
                .height(10)
                .durationMinutes(5)
                .boardId("board123")
                .powers(Set.of(Power.SPEED))
                .playerDinosaurMap(Map.of("p1", new Dinosaur()))
                .build();

        assertEquals("game1", game.getNombre());
        assertEquals(10, game.getWidth());
        assertEquals(1, game.getPlayerDinosaurMap().size());
        assertTrue(game.isActive());
    }
}
