package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {
    // send messages to the socket
    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepository;
    
    // Map con todos los jugadores agrupados por ID de juego
    private final Map<String, Map<String, Player>> activePlayers = new ConcurrentHashMap<>();

    // Scheduler para reducir vida por segundo
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> gameLoops = new ConcurrentHashMap<>();

    public void startGameLoop(String gameId) {
        if (gameLoops.containsKey(gameId)) {
            return; // Ya está corriendo
        }

        ScheduledFuture<?> loop = scheduler.scheduleAtFixedRate(() -> {
            reduceHealthOverTime(gameId); // apply damage to all players per second
        }, 0, 1, TimeUnit.SECONDS);

        gameLoops.put(gameId, loop);
    }
    
    public void stopGameLoop(String gameId) {
        ScheduledFuture<?> loop = gameLoops.remove(gameId);
        if (loop != null) loop.cancel(true);
    }



    public Player movePlayer(String gameId, String playerId, String direction) {
        Map<String, Player> gamePlayers = activePlayers.get(gameId);
        if (gamePlayers == null) return null;

        Player player = gamePlayers.get(playerId);
        if (player == null || !player.isAlive()) return null;

        synchronized (player) {
            switch (direction.toUpperCase()) {
                case "UP" -> player.setPositionY(player.getPositionY() - 1);
                case "DOWN" -> player.setPositionY(player.getPositionY() + 1);
                case "LEFT" -> player.setPositionX(player.getPositionX() - 1);
                case "RIGHT" -> player.setPositionX(player.getPositionX() + 1);
            }

            // ✅ Aquí podrías validar colisiones o límites del tablero

            // Después de mover, notificamos a todos los jugadores
            PlayerPositionDTO dto = new PlayerPositionDTO(
                    player.getId(),
                    player.getPositionX(),
                    player.getPositionY(),
                    player.getHealth(),
                    player.isAlive()
            );
            template.convertAndSend("/topic/games/" + gameId + "/players", dto);
        }

        return player;
    }

     public void reduceHealthOverTime(String gameId) {
        Map<String, Player> gamePlayers = activePlayers.get(gameId);
        if (gamePlayers == null) return;

        for (Player player : gamePlayers.values()) {
            if (player.isAlive()) {
                player.setHealth(player.getHealth() - 5);
                if (player.getHealth() <= 0) {
                    player.setAlive(false);
                }

                PlayerPositionDTO dto = new PlayerPositionDTO(
                        player.getId(),
                        player.getPositionX(),
                        player.getPositionY(),
                        player.getHealth(),
                        player.isAlive()
                );

                template.convertAndSend("/topic/games/" + gameId + "/players", dto);
            }
        }
    }


    public Game createGame(Game game) {
        // nombre es el id
        return gameRepository.save(game);
    }

    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }

    public Optional<Game> getGameById(String id) {
        return gameRepository.findById(id);
    }

    public Optional<Game> updateGame(String id, Game updated) {
        return gameRepository.findById(id).map(g -> {
            g.setActive(updated.isActive());
            g.setPlayerDinosaurMap(updated.getPlayerDinosaurMap());
            g.setPowers(updated.getPowers());
            g.setMetadata(updated.getMetadata());
            g.setDurationMinutes(updated.getDurationMinutes());
            g.setStartTime(updated.getStartTime());
            g.setTimerActive(updated.isTimerActive());
            return gameRepository.save(g);
        });
    }

    public boolean deleteGame(String id) {
        if (gameRepository.existsById(id)) {
            gameRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Optional<Game> addPlayerDinosaur(String gameId, String playerId, Dinosaur dinosaur) {
        return gameRepository.findById(gameId).flatMap(g -> {
            boolean ok = g.addPlayerDinosaur(playerId, dinosaur);
            if (!ok) {
                return Optional.empty();
            }
            return Optional.of(gameRepository.save(g));
        });
    }

    public Optional<Game> startTimer(String gameId, int minutes) {
        return gameRepository.findById(gameId).map(g -> {
            g.setDurationMinutes(minutes);
            g.setStartTime(Instant.now());
            g.setTimerActive(true);
            return gameRepository.save(g);
        });
    }

    public Optional<Game> stopTimer(String gameId) {
        return gameRepository.findById(gameId).map(g -> {
            g.setTimerActive(false);
            return gameRepository.save(g);
        });
    }

    public Optional<Long> getRemainingSeconds(String gameId) {
        return gameRepository.findById(gameId).map(g -> {
            g.refreshTimerState();
            return g.getRemainingSeconds();
        });
    }
}
