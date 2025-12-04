package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class LobbyService {

    @Autowired
    private RedisPubSubService redisPubSubService;

    private final Map<String, List<Player>> lobbies = new ConcurrentHashMap<>();

    public void addPlayer(String gameId, Player player) {
        List<Player> players = lobbies.computeIfAbsent(gameId, k -> new ArrayList<>());
        players.add(player);
        redisPubSubService.publishLobbyEvent(gameId, "players", players);
        log.info("Jugador {} agregado al lobby {}", player.getId(), gameId);
    }

    public void removePlayer(String gameId, String playerId) {
        List<Player> updatedPlayers = lobbies.computeIfPresent(gameId, (k, list) -> {
            list.removeIf(p -> p.getId().equals(playerId));
            return list;
        });

        if (updatedPlayers != null) {
            redisPubSubService.publishLobbyEvent(gameId, "players", updatedPlayers);
            log.info("Jugador {} removido del lobby {}", playerId, gameId);
        }
    }

    public List<Player> getPlayers(String gameId) {
        return lobbies.getOrDefault(gameId, Collections.emptyList());
    }

    public void clearLobby(String gameId) {
        lobbies.remove(gameId);
        log.info("Lobby {} limpiado", gameId);
    }
}