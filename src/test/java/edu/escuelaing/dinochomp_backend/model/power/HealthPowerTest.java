package edu.escuelaing.dinochomp_backend.model.power;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HealthPowerTest {

    @Test
    void testHealthPowerConstructor() {
        HealthPower healthPower = new HealthPower(20);
        assertEquals(20, healthPower.getAddedHealth());
        assertEquals("HEALTH", healthPower.getName());
    }

    @Test
    void testApplyEffect() {
        HealthPower healthPower = new HealthPower(20);
        Player player = Player.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .password("1234")
                .positionX(5)
                .positionY(10)
                .health(100)
                .isAlive(true)
                .build();
        player.setHealth(50);

        Player updatedPlayer = healthPower.applyEffect(player);

        assertEquals(70, updatedPlayer.getHealth());
    }

    @Test
    void testApplyEffect_MaxHealth() {
        HealthPower healthPower = new HealthPower(20);
        Player player = Player.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .password("1234")
                .positionX(5)
                .positionY(10)
                .health(100)
                .isAlive(true)
                .build();
        player.setHealth(90);

        Player updatedPlayer = healthPower.applyEffect(player);

        assertEquals(100, updatedPlayer.getHealth());
    }
}

