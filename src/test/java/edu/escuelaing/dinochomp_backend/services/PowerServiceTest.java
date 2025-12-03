package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.repository.PowerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PowerServiceTest {

    @Mock
    private PowerRepository powerRepository;

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PowerService powerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllPowers() {
        Power power = new HealthPower(20);
        when(powerRepository.findAll()).thenReturn(Collections.singletonList(power));
        List<Power> powers = powerService.getAllPowers();
        assertFalse(powers.isEmpty());
        assertEquals(1, powers.size());
        verify(powerRepository, times(1)).findAll();
    }

    @Test
    void testGetPowerByName() {
        Power power = new HealthPower(20);
        when(powerRepository.findById("HEALTH")).thenReturn(Optional.of(power));
        Optional<Power> foundPower = powerService.getPowerByName("HEALTH");
        assertTrue(foundPower.isPresent());
        assertEquals("HEALTH", foundPower.get().getName());
        verify(powerRepository, times(1)).findById("HEALTH");
    }

    @Test
    void testGetPowerByName_NotFound() {
        when(powerRepository.findById("UNKNOWN")).thenReturn(Optional.empty());
        Optional<Power> foundPower = powerService.getPowerByName("UNKNOWN");
        assertFalse(foundPower.isPresent());
        verify(powerRepository, times(1)).findById("UNKNOWN");
    }

    @Test
    void testSavePower() {
        Power power = new HealthPower(20);
        when(powerRepository.save(any(Power.class))).thenReturn(power);
        Power savedPower = powerService.savePower(power);
        assertNotNull(savedPower);
        assertEquals("HEALTH", savedPower.getName());
        verify(powerRepository, times(1)).save(power);
    }

    @Test
    void testDeletePower() {
        when(powerRepository.existsById("HEALTH")).thenReturn(true);
        doNothing().when(powerRepository).deleteById("HEALTH");
        boolean deleted = powerService.deletePower("HEALTH");
        assertTrue(deleted);
        verify(powerRepository, times(1)).existsById("HEALTH");
        verify(powerRepository, times(1)).deleteById("HEALTH");
    }

    @Test
    void testDeletePower_NotFound() {
        when(powerRepository.existsById("UNKNOWN")).thenReturn(false);
        boolean deleted = powerService.deletePower("UNKNOWN");
        assertFalse(deleted);
        verify(powerRepository, times(1)).existsById("UNKNOWN");
        verify(powerRepository, never()).deleteById("UNKNOWN");
    }

    @Test
    void testUpdatePower() {
        Power existingPower = new HealthPower(20);
        Power newPowerDetails = new HealthPower(30);
        when(powerRepository.findById("HEALTH")).thenReturn(Optional.of(existingPower));
        when(powerRepository.save(any(Power.class))).thenReturn(newPowerDetails);
        Optional<Power> updatedPower = powerService.updatePower("HEALTH", newPowerDetails);
        assertTrue(updatedPower.isPresent());
        verify(powerRepository, times(1)).findById("HEALTH");
        verify(powerRepository, times(1)).save(existingPower);
    }

    @Test
    void testUpdatePower_NotFound() {
        Power newPowerDetails = new HealthPower(30);
        when(powerRepository.findById("UNKNOWN")).thenReturn(Optional.empty());
        Optional<Power> updatedPower = powerService.updatePower("UNKNOWN", newPowerDetails);
        assertFalse(updatedPower.isPresent());
        verify(powerRepository, times(1)).findById("UNKNOWN");
        verify(powerRepository, never()).save(any(Power.class));
    }

    @Test
    void testCreatePower() {
        Power power = new HealthPower(20);
        when(powerRepository.save(any(Power.class))).thenReturn(power);
        Power createdPower = powerService.createPower(power);
        assertNotNull(createdPower);
        assertEquals("HEALTH", createdPower.getName());
        verify(powerRepository, times(1)).save(power);
    }

    @Test
    void testActivateRandomPower() {
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
        when(playerRepository.save(any(Player.class))).thenAnswer(i -> i.getArguments()[0]);

        Player updatedPlayer = powerService.activateRandomPower(player);

        assertNotNull(updatedPlayer);
        assertEquals(70, updatedPlayer.getHealth());
        verify(playerRepository, times(1)).save(updatedPlayer);
    }
}

