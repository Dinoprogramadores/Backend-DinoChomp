package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;

import edu.escuelaing.dinochomp_backend.utils.mappers.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class GameService {
    @Autowired
    private SimpMessagingTemplate template;

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private FoodRepository foodRepository;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private PlayerService playerService;
    @Autowired
    private BoardService boardService;
    @Autowired
    private PowerService powerService;

    private final Map<String, Map<String, Player>> activePlayers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, Boolean> powerAvailable = new ConcurrentHashMap<>();
    private final Map<String, String> powerOwner = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> healthLoops = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> powerLoops = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> syncLoops = new ConcurrentHashMap<>();
    private Map<String, ScheduledFuture<?>> connectionWindows = new ConcurrentHashMap<>();
    private final long CONNECTION_WINDOW_SECONDS = 10;

    public void registerPlayer(String gameId, Player player) {
        if (player == null || player.getId() == null) {
            throw new RuntimeException("Player inválido");
        }

        activePlayers.putIfAbsent(gameId, new ConcurrentHashMap<>());
        Map<String, Player> players = activePlayers.get(gameId);

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game no encontrado: " + gameId));
        int width = game.getWidth();
        int height = game.getHeight();

        Point[] corners = new Point[] {
                new Point(0, 0),
                new Point(width - 1, 0),
                new Point(0, height - 1),
                new Point(width - 1, height - 1)
        };

        Set<String> occupied = players.values().stream()
                .map(p -> p.getPositionX() + "," + p.getPositionY())
                .collect(Collectors.toSet());

        Point spawn = null;
        for (Point p : corners) {
            if (!occupied.contains(p.x + "," + p.y)) {
                spawn = p;
                break;
            }
        }

        player.setPositionX(spawn.x);
        player.setPositionY(spawn.y);

        // Guardar en DB antes de agregar a memoria
        playerRepository.save(player);

        players.put(player.getId(), player);

        System.out.println("Jugador " + player.getId() + " agregado a activePlayers del juego " + gameId);

        PlayerPositionDTO dto = new PlayerPositionDTO(
                player.getId(),
                player.getName(),
                player.getPositionX(),
                player.getPositionY(),
                player.getHealth(),
                player.isAlive());

        template.convertAndSend("/topic/games/" + gameId + "/players", dto);

        // NUEVO: Enviar estado actual del poder al jugador que se acaba de unir
        syncPowerStateToPlayer(gameId);
    }

    private void syncPowerStateToPlayer(String gameId) {
        Boolean isPowerAvailable = powerAvailable.get(gameId);
        String currentOwner = powerOwner.get(gameId);

        String status;
        if (Boolean.TRUE.equals(isPowerAvailable)) {
            status = "AVAILABLE";
        } else if (currentOwner != null) {
            status = "CLAIMED";
        } else {
            status = "UNAVAILABLE";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", status);
        payload.put("owner", currentOwner);
        payload.put("timestamp", Instant.now().toString());

        template.convertAndSend("/topic/games/" + gameId + "/power", payload);

        System.out.println("Estado del poder sincronizado para juego " + gameId + ": " + status);
    }

    public void startGameLoop(String gameId) {
        if (healthLoops.containsKey(gameId)) {
            return;
        }

        // LOOP 1: vida por segundo
        ScheduledFuture<?> hLoop = scheduler.scheduleAtFixedRate(
                () -> reduceHealthOverTime(gameId),
                0, 2, TimeUnit.SECONDS
        );
        healthLoops.put(gameId, hLoop);

        // LOOP 2: poderes
        ScheduledFuture<?> pLoop = scheduler.scheduleAtFixedRate(
                () -> activatePower(gameId),
                10, 10, TimeUnit.SECONDS
        );
        powerLoops.put(gameId, pLoop);

        // LOOP 3: sincronizar
        ScheduledFuture<?> sLoop = scheduler.scheduleAtFixedRate(
                () -> syncPlayersToDB(gameId),
                0, 2, TimeUnit.SECONDS
        );
        syncLoops.put(gameId, sLoop);
    }

    public void stopGameLoop(String gameId) {
        Optional.ofNullable(healthLoops.remove(gameId))
                .ifPresent(f -> f.cancel(true));

        Optional.ofNullable(powerLoops.remove(gameId))
                .ifPresent(f -> f.cancel(true));

        Optional.ofNullable(syncLoops.remove(gameId))
                .ifPresent(f -> f.cancel(true));
    }

    public Player movePlayer(String gameId, String playerId, String direction) {
        Map<String, Player> gamePlayers = activePlayers.get(gameId);
        if (gamePlayers == null)
            return null;

        Player player = gamePlayers.get(playerId);
        if (player == null || !player.isAlive())
            return null;

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("No game found"));

        int width = game.getWidth();
        int height = game.getHeight();

        int newX = player.getPositionX();
        int newY = player.getPositionY();

        switch (direction.toUpperCase()) {
            case "UP" -> newY = Math.max(0, newY - 1);
            case "DOWN" -> newY = Math.min(height - 1, newY + 1);
            case "LEFT" -> newX = Math.max(0, newX - 1);
            case "RIGHT" -> newX = Math.min(width - 1, newX + 1);
        }

        // Mover en Redis y obtener comida si la hay
        Optional<Food> eatenFood = boardService.movePlayer(game.getBoardId(), player, newX, newY);

        // CRÍTICO: Actualizar la posición en memoria también
        player.setPositionX(newX);
        player.setPositionY(newY);

        // Si comió, aumentar salud
        eatenFood.ifPresent(food -> {
            player.setHealth(player.getHealth() + food.getNutritionValue());

            Map<String, Object> foodEvent = new HashMap<>();
            foodEvent.put("action", "FOOD_REMOVED");
            foodEvent.put("id", food.getId());
            foodEvent.put("x", food.getPositionX());
            foodEvent.put("y", food.getPositionY());
            template.convertAndSend("/topic/games/" + gameId + "/food", foodEvent);
        });

        PlayerPositionDTO dto = new PlayerPositionDTO(
                player.getId(),
                player.getName(),
                player.getPositionX(),
                player.getPositionY(),
                player.getHealth(),
                player.isAlive()
        );
        template.convertAndSend("/topic/games/" + gameId + "/players", dto);

        return player;
    }

    public void reduceHealthOverTime(String gameId) {
        Map<String, Player> gamePlayers = activePlayers.get(gameId);
        if (gamePlayers == null)
            return;

        for (Player player : gamePlayers.values()) {
            if (player.isAlive()) {
                player.setHealth(player.getHealth() - 5);
                if (player.getHealth() <= 0) {
                    player.setAlive(false);
                }

                PlayerPositionDTO dto = new PlayerPositionDTO(
                        player.getId(),
                        player.getName(),
                        player.getPositionX(),
                        player.getPositionY(),
                        player.getHealth(),
                        player.isAlive());

                template.convertAndSend("/topic/games/" + gameId + "/players", dto);
            }
        }

        Optional<Long> remaining = getRemainingSeconds(gameId);
        if (remaining.isPresent() && remaining.get() <= 0) {
            endGame(gameId);
            return;
        }

        long aliveCount = gamePlayers.values().stream()
                .filter(Player::isAlive)
                .count();

        if (aliveCount <= 1) {
            computeAndSetWinner(gameId);
            endGame(gameId);
        }
    }

    public void activatePower(String gameId) {
        if (Boolean.TRUE.equals(powerAvailable.get(gameId))) {
            return;
        }

        powerAvailable.put(gameId, true);
        powerOwner.remove(gameId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "AVAILABLE");
        payload.put("owner", null);
        payload.put("timestamp", Instant.now().toString());

        template.convertAndSend("/topic/games/" + gameId + "/power", payload);
    }

    public synchronized void claimPower(String gameId, String playerId) {
        if (!Boolean.TRUE.equals(powerAvailable.get(gameId))) {
            return;
        }
        powerAvailable.put(gameId, false);
        powerOwner.put(gameId, playerId);
        usePower(gameId, playerId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "CLAIMED");
        payload.put("owner", playerId);
        payload.put("timestamp", Instant.now().toString());

        template.convertAndSend("/topic/games/" + gameId + "/power", payload);
    }

    public void usePower(String gameId, String playerId) {
        String currentOwner = powerOwner.get(gameId);
        if (!playerId.equals(currentOwner)) {
            System.out.println("Jugador " + playerId + " intentó usar un poder que no tiene");
            return;
        }

        Player player = playerRepository.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found to apply the power"));

        Player updatedPlayer = powerService.activateRandomPower(player);

        // Actualizar en memoria
        Player inMemory = activePlayers.get(gameId).get(playerId);
        if (inMemory != null) {
            inMemory.setHealth(updatedPlayer.getHealth());
        }

        powerOwner.remove(gameId);
        powerAvailable.put(gameId, false);

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "USED");
        payload.put("owner", playerId);
        payload.put("timestamp", Instant.now().toString());

        template.convertAndSend("/topic/games/" + gameId + "/power", payload);
    }

    public Game createGame(Game game, int totalFood) {
        if (game == null)
            throw new IllegalArgumentException("game required");

        Board board = boardService.createBoard(game.getWidth(), game.getHeight());
        game.setBoardId(board.getId());

        if (totalFood > 0) {
            seedFood(game.getBoardId(), game.getWidth(), game.getHeight(), totalFood);
        }

        Game saved = gameRepository.save(game);
        openConnectionWindow(saved.getNombre());

        return saved;
    }

    public void openConnectionWindow(String gameId) {
        ScheduledFuture<?> timer = scheduler.schedule(() -> {
            handleConnectionWindowEnd(gameId);
        }, CONNECTION_WINDOW_SECONDS, TimeUnit.SECONDS);

        connectionWindows.put(gameId, timer);
    }

    private void handleConnectionWindowEnd(String gameId) {
        Map<String, Player> players = activePlayers.get(gameId);
        int count = (players != null) ? players.size() : 0;

        if (count >= 2) {
            startGameLoop(gameId);
        } else {
            stopGameLoop(gameId);
        }

        connectionWindows.remove(gameId);
    }

    // CORREGIDO: seedFood ahora trabaja directamente con BoardService
    private void seedFood(String boardId, int width, int height, int totalFood) {
        if (width <= 2 || height <= 2 || totalFood <= 0)
            return;

        Random random = new Random();
        int placed = 0;
        int maxAttempts = totalFood * 5;
        int attempts = 0;

        // Obtener el board actual para verificar posiciones ocupadas
        Optional<Board> boardOpt = boardService.getBoard(boardId);
        if (boardOpt.isEmpty()) {
            throw new RuntimeException("Board not found: " + boardId);
        }
        Board board = boardOpt.get();

        while (placed < totalFood && attempts < maxAttempts) {
            attempts++;

            int x = random.nextInt(width);
            int y = random.nextInt(height);

            // Evitar esquinas
            boolean isCorner = (x == 0 && y == 0)
                    || (x == 0 && y == height - 1)
                    || (x == width - 1 && y == 0)
                    || (x == width - 1 && y == height - 1);
            if (isCorner)
                continue;

            Point p = new Point(x, y);

            // Verificar si está ocupado
            if (!board.isNull(p))
                continue;

            // Crear Food
            Food f = Food.builder()
                    .name("pollo")
                    .positionX(x)
                    .positionY(y)
                    .nutritionValue(10)
                    .build();

            // Agregar usando BoardService (guarda en Redis y DB)
            boardService.addFood(boardId, f);

            // Actualizar el board local para las próximas iteraciones
            board.getMap().put(p, f);

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

    public Optional<Game> addPlayerDinosaur(String gameId, String playerId, Dinosaur dinosaur) {
        return gameRepository.findById(gameId).flatMap(g -> {
            boolean ok = g.addPlayerDinosaur(playerId, dinosaur);

            if (!ok) {
                return Optional.empty();
            }

            // IMPORTANTE: Primero buscar/guardar el Player en DB
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new RuntimeException("Player no encontrado: " + playerId));

            // Asegurar que el player esté guardado con su posición actual
            playerRepository.save(player);

            Game game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new RuntimeException("Game no encontrado: " + gameId));

            try {
                // Ahora sí agregar al board (Redis + DB)
                boardService.addPlayer(game.getBoardId(), player);
            } catch (Exception e) {
                throw new RuntimeException("Error agregando player al board: " + e.getMessage(), e);
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

    public Optional<Player> getWinner(String gameId) {
        return gameRepository.findById(gameId).map(Game::getWinner);
    }

    public Optional<Player> computeAndSetWinner(String gameId) {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isEmpty())
            return Optional.empty();
        Game game = optGame.get();

        Map<String, Dinosaur> pdm = game.getPlayerDinosaurMap();
        if (pdm == null || pdm.isEmpty()) {
            game.setWinner(null);
            gameRepository.save(game);
            return Optional.empty();
        }

        Set<String> playerIds = pdm.keySet();
        List<Player> playersInGame = new ArrayList<>(playerRepository.findAllById(playerIds));
        if (playersInGame.isEmpty()) {
            game.setWinner(null);
            gameRepository.save(game);
            return Optional.empty();
        }

        List<Player> alive = playersInGame.stream()
                .filter(Player::isAlive)
                .collect(Collectors.toList());

        Player winner;
        if (alive.size() == 1) {
            winner = alive.get(0);
        } else if (alive.size() > 1) {
            winner = pickByHighestHealthThenFirst(alive);
        } else {
            winner = pickByHighestHealthThenFirst(playersInGame);
        }

        game.setWinner(winner);
        gameRepository.save(game);
        return Optional.ofNullable(winner);
    }

    private Player pickByHighestHealthThenFirst(List<Player> candidates) {
        if (candidates.isEmpty())
            return null;

        int maxHealth = candidates.stream()
                .mapToInt(Player::getHealth)
                .max()
                .orElse(Integer.MIN_VALUE);

        List<Player> maxes = candidates.stream()
                .filter(p -> p.getHealth() == maxHealth)
                .collect(Collectors.toList());

        if (maxes.size() == 1)
            return maxes.get(0);

        return maxes.stream()
                .min(Comparator.comparing(Player::getId))
                .orElse(maxes.get(0));
    }

    private void syncPlayersToDB(String gameId) {
        Map<String, Player> players = activePlayers.get(gameId);
        if (players == null)
            return;

        for (Player p : players.values()) {
            try {
                playerRepository.save(p);
            } catch (Exception e) {
                System.out.println("Error sincronizando jugador " + p.getId() + ": " + e.getMessage());
            }
        }
    }

    public void endGame(String gameId) {
        Optional<Game> gameOpt = getGameById(gameId);
        if (gameOpt.isEmpty()) {
            return;
        }

        Game game = gameOpt.get();
        String winner = (game.getWinner() != null) ? game.getWinner().getName() : "Ninguno";

        template.convertAndSend(
                "/topic/games/" + gameId + "/events",
                Map.of(
                        "event", "GAME_ENDED",
                        "winner", winner
                )
        );

        stopGameLoop(gameId);

        activePlayers.remove(gameId);
        powerAvailable.remove(gameId);
        powerOwner.remove(gameId);

        System.out.println("Partida " + gameId + " finalizada correctamente");
    }
}