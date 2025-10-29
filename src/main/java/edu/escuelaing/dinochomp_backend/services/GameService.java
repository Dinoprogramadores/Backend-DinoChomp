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
    // send messages to the socket
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

    // Map con todos los jugadores agrupados por ID de juego
    private final Map<String, Map<String, Player>> activePlayers = new ConcurrentHashMap<>();

    // Scheduler para reducir vida por segundo
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<String, ScheduledFuture<?>> gameLoops = new ConcurrentHashMap<>();
    // poderes disponibles por juego
    private final Map<String, Boolean> powerAvailable = new ConcurrentHashMap<>();
    // jugador que tiene el poder activo por juego
    private final Map<String, String> powerOwner = new ConcurrentHashMap<>();

    public void registerPlayer(String gameId, Player player) {
        if (player == null || player.getId() == null) {
            throw new RuntimeException("Player inválido");
        }

        // Si no existe el mapa del juego, lo crea
        activePlayers.putIfAbsent(gameId, new ConcurrentHashMap<>());

        // Agrega o actualiza al jugador
        activePlayers.get(gameId).put(player.getId(), player);

        System.out.println("✅ Jugador " + player.getId() + " agregado a activePlayers del juego " + gameId);

        // Notifica a todos los jugadores conectados al juego
        PlayerPositionDTO dto = new PlayerPositionDTO(
                player.getId(),
                player.getName(),
                player.getPositionX(),
                player.getPositionY(),
                player.getHealth(),
                player.isAlive());

        template.convertAndSend("/topic/games/" + gameId + "/players", dto);
    }

    public void startGameLoop(String gameId) {
        if (gameLoops.containsKey(gameId)) {
            return; // Ya está corriendo
        }
        // hilo que reduce vida cada segundo
        ScheduledFuture<?> loop = scheduler.scheduleAtFixedRate(() -> {
            reduceHealthOverTime(gameId); // aplica daño a todos los jugadores de la partida
        }, 0, 1, TimeUnit.SECONDS);

        gameLoops.put(gameId, loop);
        // hilo que activa poderes cada 30 segundos
        scheduler.scheduleAtFixedRate(() -> {
            activatePower(gameId);
        }, 10, 30, TimeUnit.SECONDS);
    }

    public void stopGameLoop(String gameId) {
        ScheduledFuture<?> loop = gameLoops.remove(gameId);
        if (loop != null)
            loop.cancel(true);
    }

    public Player movePlayer(String gameId, String playerId, String direction) {
        Map<String, Player> gamePlayers = activePlayers.get(gameId);
        int height = 6;
        int width = 10;
        if (gamePlayers == null)
            return null;
        Player player = gamePlayers.get(playerId);
        if (player == null || !player.isAlive())
            return null;

        synchronized (player) {
            switch (direction.toUpperCase()) {
                case "UP" -> player.setPositionY(Math.max(0, player.getPositionY() - 1));
                case "DOWN" -> player.setPositionY(Math.min(height - 1, player.getPositionY() + 1));
                case "LEFT" -> player.setPositionX(Math.max(0, player.getPositionX() - 1));
                case "RIGHT" -> player.setPositionX(Math.min(width - 1, player.getPositionX() + 1));
            }

            // Después de mover, notificamos a todos los jugadores
            PlayerPositionDTO dto = new PlayerPositionDTO(
                    player.getId(),
                    player.getName(),
                    player.getPositionX(),
                    player.getPositionY(),
                    player.getHealth(),
                    player.isAlive());
            template.convertAndSend("/topic/games/" + gameId + "/players", dto);
        }

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
    }

    public void activatePower(String gameId) {
        if (Boolean.TRUE.equals(powerAvailable.get(gameId))) {
            return; // Ya hay un poder activo
        }

        powerAvailable.put(gameId, true);
        powerOwner.remove(gameId);

        template.convertAndSend("/topic/games/" + gameId + "/power", "AVAILABLE");
        System.out.println("⚡ Poder activado en juego " + gameId);
    }

    // Nuevo: crear juego y sembrar comida aleatoria según totalFood
    public Game createGame(Game game, int totalFood) {
        if (game == null) throw new IllegalArgumentException("game required");
        System.out.println("getWidth: " + game.getWidth() + "getHeight: " + game.getHeight());
        Board board = boardService.createBoard(game.getWidth(), game.getHeight());
        game.setBoardId(board.getId());
        System.out.println("Partida: " + game.getNombre() + "Tablero: " + game.getBoardId());
        if (totalFood > 0) {
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
    // Obtener el winner almacenado en el Game
    public Optional<Player> getWinner(String gameId) {
        return gameRepository.findById(gameId).map(Game::getWinner);
    }

    // Calcular y establecer el winner según reglas, y retornar el ganador
    public Optional<Player> computeAndSetWinner(String gameId) {
        Optional<Game> optGame = gameRepository.findById(gameId);
        if (optGame.isEmpty()) return Optional.empty();
        Game game = optGame.get();

        Map<String, Dinosaur> pdm = game.getPlayerDinosaurMap();
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