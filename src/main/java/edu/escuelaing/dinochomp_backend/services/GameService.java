package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import edu.escuelaing.dinochomp_backend.utils.dto.PlayerPositionDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Service
public class GameService {
    // send messages to the socket
    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepository;
    // Map to hold scheduled game loops
    private final Map<String, java.util.concurrent.ScheduledFuture<?>> gameLoops = new ConcurrentHashMap<>();
    // Scheduler for game loops
    private final java.util.concurrent.ScheduledExecutorService scheduler = java.util.concurrent.Executors
            .newScheduledThreadPool(4);

    private final Map<String, Player> players = new ConcurrentHashMap<>();

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
        if (loop != null) {
            loop.cancel(true);
        }
    }

    public Player addPlayer(String id) {
        Player player = new Player();
        players.put(id, player);
        return player;
    }

    public Player movePlayer(String id, String direction) {
        Player player = players.get(id);
        if (player != null && player.isAlive()) {
            synchronized (player) {
                player.move(direction);
            }
        }
        return player;
    }

    public void reduceHealthOverTime(String gameId) {
        for (Player player : players.values()) {
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
                        player.isAlive());

                template.convertAndSend("/topic/games/" + gameId + "/players", dto);
            }
        }
    }

    public void decreaseHealthForAll() {
        for (Player p : players.values()) {
            p.loseHealth(1);
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
