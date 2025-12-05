package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class LobbyService {

    private final RedisPubSubService redisPubSubService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<String, List<Player>> lobbies = new ConcurrentHashMap<>();

    public void addPlayer(String gameId, Player player) {
        List<Player> players = lobbies.computeIfAbsent(gameId, k -> new ArrayList<>());
        players.add(player);
        
        // ✅ Publicar cifrado
        publishEncryptedPlayers(gameId, players);
        
        log.info("Jugador {} agregado al lobby {}", player.getId(), gameId);
    }

    public void removePlayer(String gameId, String playerId) {
        List<Player> updatedPlayers = lobbies.computeIfPresent(gameId, (k, list) -> {
            list.removeIf(p -> p.getId().equals(playerId));
            return list;
        });

        if (updatedPlayers != null) {
            // ✅ Publicar cifrado
            publishEncryptedPlayers(gameId, updatedPlayers);
            
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

    /**
     * 🔐 Cifra y publica la lista de jugadores
     */
    private void publishEncryptedPlayers(String gameId, List<Player> players) {
        try {
            // 1. Serializar a JSON
            String json = mapper.writeValueAsString(players);
            
            // 2. Cifrar
            AesCrypto.Encrypted encrypted = AesCrypto.encrypt(json);
            
            // 3. Crear payload con iv y ciphertext
            Map<String, String> payload = Map.of(
                "iv", encrypted.iv(),
                "ciphertext", encrypted.ciphertext()
            );
            
            // 4. Publicar
            redisPubSubService.publishLobbyEvent(gameId, "players", payload);
            
            log.debug("🔐 Lista de jugadores cifrada publicada en lobby {}", gameId);
            
        } catch (Exception e) {
            log.error("❌ Error cifrando lista de jugadores del lobby {}", gameId, e);
        }
    }
}