package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LobbyService {

    private final Map<String, List<Player>> lobbies = new ConcurrentHashMap<>();

    public void addPlayer(String gameId, Player player) {
        lobbies.computeIfAbsent(gameId, k -> new ArrayList<>()).add(player);
    }

    public void removePlayer(String gameId, String playerId) {
        lobbies.computeIfPresent(gameId, (k, list) -> {
            list.removeIf(p -> p.getId().equals(playerId));
            return list;
        });
    }

    public List<Player> getPlayers(String gameId) {
        return lobbies.getOrDefault(gameId, Collections.emptyList());
    }
}