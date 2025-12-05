package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LobbyServiceTest {

    @Mock
    private RedisPubSubService redisPubSubService;

    private LobbyService lobbyService;

    @BeforeEach
    void setUp() {
        lobbyService = new LobbyService(redisPubSubService);
    }

    @Test
    void getPlayers_whenLobbyNotExist_returnsEmptyList() {
        List<Player> players = lobbyService.getPlayers("unknown");
        assertNotNull(players);
        assertTrue(players.isEmpty());
    }

    @Test
    void addPlayer_createsLobbyAndAddsPlayer() {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");
        p1.setName("Alice");

        lobbyService.addPlayer(gameId, p1);

        List<Player> players = lobbyService.getPlayers(gameId);
        assertEquals(1, players.size());
        assertEquals("p1", players.get(0).getId());
        assertEquals("Alice", players.get(0).getName());
        verify(redisPubSubService).publishLobbyEvent(eq(gameId), eq("players"), any(Map.class));
    }

    @Test
    void addMultiplePlayers_preservesAll() {
        String gameId = "g1";
        Player p1 = new Player(); p1.setId("p1");
        Player p2 = new Player(); p2.setId("p2");

        lobbyService.addPlayer(gameId, p1);
        lobbyService.addPlayer(gameId, p2);

        List<Player> players = lobbyService.getPlayers(gameId);
        assertEquals(2, players.size());
        assertTrue(players.stream().anyMatch(p -> "p1".equals(p.getId())));
        assertTrue(players.stream().anyMatch(p -> "p2".equals(p.getId())));
    }


    @Test
    void removePlayer_nonExisting_doesNothing() {
        String gameId = "g1";
        Player p1 = new Player(); p1.setId("p1");
        lobbyService.addPlayer(gameId, p1);

        lobbyService.removePlayer(gameId, "pX");

        List<Player> players = lobbyService.getPlayers(gameId);
        assertEquals(1, players.size());
        assertEquals("p1", players.get(0).getId());
    }

    @Test
    void operationsAreScopedPerGameId() {
        Player a = new Player(); a.setId("a");
        Player b = new Player(); b.setId("b");
        lobbyService.addPlayer("g1", a);
        lobbyService.addPlayer("g2", b);

        List<Player> g1Players = lobbyService.getPlayers("g1");
        List<Player> g2Players = lobbyService.getPlayers("g2");
        assertEquals(1, g1Players.size());
        assertEquals("a", g1Players.get(0).getId());
        assertEquals(1, g2Players.size());
        assertEquals("b", g2Players.get(0).getId());
    }

    @Test
    void clearLobby_removesLobby() {
        String gameId = "g1";
        Player p1 = new Player();
        p1.setId("p1");
        lobbyService.addPlayer(gameId, p1);

        lobbyService.clearLobby(gameId);

        List<Player> players = lobbyService.getPlayers(gameId);
        assertTrue(players.isEmpty());
    }
}
