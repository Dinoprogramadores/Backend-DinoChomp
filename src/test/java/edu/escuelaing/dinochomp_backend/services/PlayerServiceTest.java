package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSavePlayer() {
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

        when(playerRepository.save(any(Player.class))).thenReturn(player);

        Player createdPlayer = playerService.savePlayer(player);

        assertNotNull(createdPlayer);
        assertEquals("John", createdPlayer.getName());
        verify(playerRepository, times(1)).save(player);
    }

    @Test
    void testGetPlayerById() {
        Player player = Player.builder().id("1").name("John").build();
        when(playerRepository.findById("1")).thenReturn(Optional.of(player));

        Optional<Player> found = playerService.getPlayerById("1");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getName());
        verify(playerRepository, times(1)).findById("1");
    }

    @Test
    void testGetPlayerByEmail() {
        Player player = Player.builder().email("john@test.com").name("John").build();
        when(playerRepository.findByEmail("john@test.com")).thenReturn(Optional.of(player));

        Optional<Player> found = playerService.getPlayerByEmail("john@test.com");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getName());
        verify(playerRepository, times(1)).findByEmail("john@test.com");
    }

    @Test
    void testUpdatePlayer() {
        Player existing = Player.builder()
                .id("1")
                .name("John")
                .email("old@test.com")
                .password("0000")
                .positionX(0)
                .positionY(0)
                .health(50)
                .isAlive(true)
                .build();

        Player updated = Player.builder()
                .name("Johnny")
                .email("new@test.com")
                .password("1234")
                .positionX(10)
                .positionY(20)
                .health(100)
                .isAlive(false)
                .build();

        when(playerRepository.findById("1")).thenReturn(Optional.of(existing));
        when(playerRepository.save(any(Player.class))).thenReturn(existing);

        Optional<Player> result = playerService.updatePlayer("1", updated);

        assertTrue(result.isPresent());
        assertEquals("Johnny", result.get().getName());
        assertEquals("new@test.com", result.get().getEmail());
        assertEquals(100, result.get().getHealth());
        assertFalse(result.get().isAlive());

        verify(playerRepository, times(1)).findById("1");
        verify(playerRepository, times(1)).save(existing);
    }

    @Test
    void testUpdatePlayer_NotFound() {
        Player updated = Player.builder().name("Johnny").build();

        when(playerRepository.findById("1")).thenReturn(Optional.empty());

        Optional<Player> result = playerService.updatePlayer("1", updated);

        assertFalse(result.isPresent());
        verify(playerRepository, times(1)).findById("1");
        verify(playerRepository, never()).save(any(Player.class));
    }

    @Test
    void testDeletePlayer() {
        when(playerRepository.existsById("1")).thenReturn(true);
        doNothing().when(playerRepository).deleteById("1");

        boolean deleted = playerService.deletePlayer("1");

        assertTrue(deleted);
        verify(playerRepository, times(1)).existsById("1");
        verify(playerRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeletePlayer_NotFound() {
        when(playerRepository.existsById("1")).thenReturn(false);

        boolean deleted = playerService.deletePlayer("1");

        assertFalse(deleted);
        verify(playerRepository, times(1)).existsById("1");
        verify(playerRepository, never()).deleteById(anyString());
    }

    @Test
    void testGetAllPlayers() {
        when(playerRepository.findAll()).thenReturn(List.of(new Player(), new Player()));

        List<Player> players = playerService.getAllPlayers();

        assertEquals(2, players.size());
        verify(playerRepository, times(1)).findAll();
    }
}
