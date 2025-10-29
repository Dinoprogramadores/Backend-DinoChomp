package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.utils.dto.game.AddPlayerDinosaurGame;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;

import edu.escuelaing.dinochomp_backend.utils.mappers.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.awt.Point;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GameService {
    // send messages to the socket
    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BoardRepository boardRepository;

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

    // Nuevo: crear juego y sembrar comida aleatoria según totalFood
    public Game createGame(Game game, int totalFood) {
        if (game == null) throw new IllegalArgumentException("game required");
        Board board = game.getBoard();
        if (board != null && totalFood > 0) {
            seedFood(board, totalFood);
        }
        System.out.println("DEBUG MAP: " + game.getPlayerDinosaurMap());
        boardRepository.save(BoardMapper.toDocument(board));
        return gameRepository.save(game);
    }

    private void seedFood(Board board, int totalFood) {
        int width = board.getWidth();
        int height = board.getHeight();

        if (width <= 2 || height <= 2 || totalFood <= 0) return;

        Random random = new Random();
        int placed = 0;
        int maxAttempts = totalFood * 5; // evita bucles infinitos si el tablero está lleno
        int attempts = 0;

        while (placed < totalFood && attempts < maxAttempts) {
            attempts++;

            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // Evitar esquinas
            boolean isCorner = (x == 0 && y == 0)
                    || (x == 0 && y == height - 1)
                    || (x == width - 1 && y == 0)
                    || (x == width - 1 && y == height - 1);
            if (isCorner) continue;

            Point p = new Point(x, y);
            if (!board.isNull(p)) continue; // ya ocupado

            // Crear y agregar el Food
            Food f = Food.builder()
                    .name("pollo")
                    .positionX(x)
                    .positionY(y)
                    .nutritionValue(10)
                    .build();

            board.addFood(f);
            foodRepository.save(f);
            placed++;
        }
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

    public Optional<Game> addPlayerDinosaur(AddPlayerDinosaurGame addPlayer) {
        return gameRepository.findById(addPlayer.getGameId()).flatMap(g -> {
            boolean ok = g.addPlayerDinosaur(addPlayer.getPlayerId(), addPlayer.getDinosaurName());
            if (!ok) {
                return Optional.empty();
            }
            return Optional.of(gameRepository.save(g));
        });
    }

    // Nuevo: eliminar del mapa por nombre de jugador
    public boolean removePlayerFromGameByName(String gameId, String playerName) {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isEmpty()) return false;
        Game game = optGame.get();

        // Buscar players por nombre y tomar el primero
        List<Player> playersByName = playerRepository.findByName(playerName);
        if (playersByName == null || playersByName.isEmpty()) return false;
        String playerId = playersByName.get(0).getId();

        boolean removed = game.removePlayerDinosaur(playerId);
        if (removed) {
            gameRepository.save(game);
        }
        return removed;
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

    // Nuevo: obtener el mapa playerId -> dinosaurio por id de juego
    public Optional<Map<String, String>> getPlayerDinosaurMap(String gameId) {
        return gameRepository.findById(gameId).map(Game::getPlayerDinosaurMap);
    }

    // Obtener el winner almacenado en el Game
    public Optional<Player> getWinner(String gameId) {
        return gameRepository.findById(gameId).map(Game::getWinner);
    }

    // Calcular y establecer el winner según reglas, y retornar el ganador
    public Optional<Player> computeAndSetWinner(String gameId) {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isEmpty()) return Optional.empty();
        Game game = optGame.get();

        Map<String, String> pdm = game.getPlayerDinosaurMap();
        if (pdm == null || pdm.isEmpty()) {
            game.setWinner(null);
            gameRepository.save(game);
            return Optional.empty();
        }

        Set<String> playerIds = pdm.keySet();
        // findAllById devuelve Iterable, lo convertimos a lista
        List<Player> playersInGame = new ArrayList<>(playerRepository.findAllById(playerIds));
        if (playersInGame.isEmpty()) {
            game.setWinner(null);
            gameRepository.save(game);
            return Optional.empty();
        }

        List<Player> alive = playersInGame.stream().filter(Player::isAlive).collect(Collectors.toList());
        Player winner;
        if (alive.size() == 1) {
            winner = alive.get(0);
        } else if (alive.size() > 1) {
            winner = pickByHighestHealthThenFirst(alive);
        } else { // nadie vivo: elegir por mayor health entre todos
            winner = pickByHighestHealthThenFirst(playersInGame);
        }

        game.setWinner(winner);
        gameRepository.save(game);
        return Optional.ofNullable(winner);
    }

    private Player pickByHighestHealthThenFirst(List<Player> candidates) {
        if (candidates.isEmpty()) return null;
        int maxHealth = candidates.stream().mapToInt(Player::getHealth).max().orElse(Integer.MIN_VALUE);
        List<Player> maxes = candidates.stream()
                .filter(p -> p.getHealth() == maxHealth)
                .collect(Collectors.toList());
        if (maxes.size() == 1) return maxes.get(0);
        // Desempate: elegir el "primero" determinístico. Usamos id ascendente para consistencia.
        return maxes.stream().min(Comparator.comparing(Player::getId)).orElse(maxes.get(0));
    }
}
