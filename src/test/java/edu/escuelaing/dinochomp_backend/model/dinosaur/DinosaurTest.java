package edu.escuelaing.dinochomp_backend.model.dinosaur;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DinosaurTest {

    @Test
    void testDinosaurBuilder() {
        Dinosaur dinosaur = Dinosaur.builder()
                .id("1")
                .name("T-Rex")
                .damage(100)
                .build();

        assertEquals("1", dinosaur.getId());
        assertEquals("T-Rex", dinosaur.getName());
        assertEquals(100, dinosaur.getDamage());
    }

    @Test
    void testDinosaurSetters() {
        Dinosaur dinosaur = new Dinosaur();
        dinosaur.setId("2");
        dinosaur.setName("Raptor");
        dinosaur.setDamage(80);

        assertEquals("2", dinosaur.getId());
        assertEquals("Raptor", dinosaur.getName());
        assertEquals(80, dinosaur.getDamage());
    }
}

