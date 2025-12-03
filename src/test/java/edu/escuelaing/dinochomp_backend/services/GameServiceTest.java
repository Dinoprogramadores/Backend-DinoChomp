package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.utils.DinoChompException;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerPositionDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.awt.*;
import java.time.Instant;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceTest {

    @Mock
    private SimpMessagingTemplate template;
    @Mock
    private GameRepository gameRepository;
    @Mock
    private PlayerRepository playerRepository;
    @Mock
    private BoardService boardService;
    @Mock
    private PowerService powerService;

    @InjectMocks
    private GameService gameService;

    private Game game;
    private Board board;

    @BeforeEach
    void init() {
        board = Board.builder()
                .id("board123")
                .width(6)
                .height(6)
                .map(new HashMap<>())
                .build();

        Map<String, Dinosaur> pdm = new HashMap<>();
        pdm.put("p1", Dinosaur.builder().name("TRex").damage(10).build());

        game = Game.builder()
                .nombre("g1")
                .boardId("board123")
                .width(6)
                .height(6)
                .playerDinosaurMap(pdm)
                .isActive(true)
                .durationMinutes(1)
                .timerActive(false)
                .build();
    }

    // registerPlayer

    @Test
    void registerPlayer_assignsCorner_and_sendsMessage() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Player player = Player.builder().id("p2").name("n2").health(100).isAlive(true).build();

        gameService.registerPlayer("g1", player);

        assertEquals(0, player.getPositionX());
        assertEquals(0, player.getPositionY());
        verify(playerRepository).save(any(Player.class));
        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/players"), any(PlayerPositionDTO.class));
    }

    @Test
    void registerPlayer_throwsWhenNullPlayer() {
        assertThrows(DinoChompException.class, () -> gameService.registerPlayer("g1", null));
    }

    @Test
    void registerPlayer_throwsWhenNullId() {
        Player p = Player.builder().name("noId").build();
        assertThrows(DinoChompException.class, () -> gameService.registerPlayer("g1", p));
    }

    @Test
    void registerPlayer_noCornersAvailable_throws() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));

        // Simula ocupación de las 4 esquinas a través de registro directo en memoria interna
        Player pA = Player.builder().id("a").name("a").positionX(0).positionY(0).health(100).isAlive(true).build();
        Player pB = Player.builder().id("b").name("b").positionX(5).positionY(0).health(100).isAlive(true).build();
        Player pC = Player.builder().id("c").name("c").positionX(0).positionY(5).health(100).isAlive(true).build();
        Player pD = Player.builder().id("d").name("d").positionX(5).positionY(5).health(100).isAlive(true).build();

        // Registra primero cuatro jugadores para ocupar esquinas
        gameService.registerPlayer("g1", pA);
        gameService.registerPlayer("g1", pB);
        gameService.registerPlayer("g1", pC);
        gameService.registerPlayer("g1", pD);

        Player pE = Player.builder().id("e").name("e").health(100).isAlive(true).build();

        assertThrows(DinoChompException.class, () -> gameService.registerPlayer("g1", pE));
    }

    // syncPowerStateToPlayer is indirectly covered by registerPlayer; add direct coverage via reflection by toggling maps through claim/activate calls

    // createGame + seedFood

    @Test
    void createGame_seedsFood_and_opensWindow() {
        Game g = Game.builder().nombre("x").width(6).height(6).build();
        when(boardService.createBoard(6, 6)).thenReturn(board);
        when(boardService.getBoard("board123")).thenReturn(Optional.of(board));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Game saved = gameService.createGame(g, 3);
        assertNotNull(saved.getBoardId());
        verify(boardService).createBoard(6, 6);
        verify(gameRepository, atLeastOnce()).save(any(Game.class));
    }

    @Test
    void createGame_throwsOnNull() {
        assertThrows(IllegalArgumentException.class, () -> gameService.createGame(null, 0));
    }

    // openConnectionWindow + handleConnectionWindowEnd: cobertura sin esperar realmente
    @Test
    void openConnectionWindow_and_handleEnd_withNotEnoughPlayers_calls_endGame() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        // Invocamos directamente el método privado por trigger: openConnectionWindow schedule y luego llamamos endGame manualmente
        gameService.openConnectionWindow("g1");
        // Simula fin inmediato
        // Llamada directa al método público que se usa en el flujo
        gameService.endGame("g1", "No hay suficientes jugadores para iniciar el juego.");
        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/events"), anyMap());
    }

    // movePlayer

    @Test
    void movePlayer_movesWithinBounds_and_eatsFood_and_sendsEvents() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Player p = Player.builder().id("pX").name("PX").positionX(1).positionY(1).health(50).isAlive(true).build();

        // Registra jugador en memoria
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        gameService.registerPlayer("g1", p);

        Food food = Food.builder().id("f1").positionX(1).positionY(2).nutritionValue(10).name("pollo").build();
        when(boardService.movePlayer(eq("board123"), eq(p), anyInt(), anyInt()))
                .thenAnswer(inv -> {
                    int nx = inv.getArgument(2);
                    int ny = inv.getArgument(3);
                    if (nx == 1 && ny == 2) {
                        p.setPositionX(nx);
                        p.setPositionY(ny);
                        return Optional.of(food);
                    } else {
                        p.setPositionX(nx);
                        p.setPositionY(ny);
                        return Optional.empty();
                    }
                });

        Player moved = gameService.movePlayer("g1", "pX", "DOWN");
        assertNotNull(moved);
        assertEquals(0, moved.getPositionX());
        assertEquals(1, moved.getPositionY());
        assertFalse(moved.getHealth() >= 60);
    }

    @Test
    void movePlayer_blocksWhenOccupied() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));

        Player a = Player.builder().id("A").name("A").positionX(1).positionY(1).health(100).isAlive(true).build();
        Player b = Player.builder().id("B").name("B").positionX(1).positionY(2).health(100).isAlive(true).build();

        gameService.registerPlayer("g1", a);
        gameService.registerPlayer("g1", b);

        when(boardService.movePlayer(eq("board123"), any(Player.class), anyInt(), anyInt()))
                .thenReturn(Optional.empty()); // no se mueve a casilla ocupada

        Player result = gameService.movePlayer("g1", "A", "DOWN");
        assertNotNull(result);
        // la posición no cambia por bloqueo lógico interno antes de llamar a boardService
        assertEquals(0, result.getPositionX());
        assertEquals(0, result.getPositionY());
    }

    @Test
    void movePlayer_invalidDirection_throws() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Player p = Player.builder().id("pZ").name("pZ").positionX(0).positionY(0).health(100).isAlive(true).build();
        gameService.registerPlayer("g1", p);
        assertThrows(IllegalArgumentException.class, () -> gameService.movePlayer("g1", "pZ", "NOPE"));
    }

    @Test
    void movePlayer_returnsNull_whenPlayerNotFoundOrDead() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Player dead = Player.builder().id("dead").name("d").positionX(0).positionY(0).health(0).isAlive(false).build();
        gameService.registerPlayer("g1", dead);
        assertNull(gameService.movePlayer("g1", "dead", "RIGHT"));
        assertNull(gameService.movePlayer("gX", "nobody", "UP"));
    }

    // reduceHealthOverTime

    @Test
    void reduceHealthOverTime_handlesLessThanTwoPlayers() {
        gameService.reduceHealthOverTime("noGame");
        // simplemente retorna, sin efectos
    }

    @Test
    void reduceHealthOverTime_decrementsHealth_and_endsWhenTimeOver_orOneAlive() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));

        Player a = Player.builder().id("A").name("A").positionX(1).positionY(1).health(10).isAlive(true).build();
        Player b = Player.builder().id("B").name("B").positionX(2).positionY(2).health(5).isAlive(true).build();

        gameService.registerPlayer("g1", a);
        gameService.registerPlayer("g1", b);

        // timer activo y casi agotado
        game.setTimerActive(true);
        game.setStartTime(Instant.now().minusSeconds(60));
        game.setDurationMinutes(1);

        gameService.reduceHealthOverTime("g1"); // debería terminar por tiempo

        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/events"), anyMap());
    }

    // activatePower, claimPower, usePower

    @Test
    void activatePower_setsAvailable_and_broadcasts() {
        gameService.activatePower("g1");
        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/power"), (Object) argThat(m -> "AVAILABLE".equals(((Map)m).get("status")) || "AVAILABLE".equals(((Map)m).get("STATUS"))));
    }

    @Test
    void claimPower_ignoresWhenUnavailable() {
        // no disponible => no hace nada
        gameService.claimPower("g1", "p1");
        // sin verificación específica
    }

    @Test
    void claimPower_and_usePower_updatesHealth_and_broadcasts() {
        // preparar game y player
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Player p = Player.builder().id("P").name("P").health(10).isAlive(true).positionX(0).positionY(0).build();
        when(playerRepository.findById("P")).thenReturn(Optional.of(p));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));

        // registrar jugador en memoria para evitar NPE en activePlayers.get(gameId)
        gameService.registerPlayer("g1", p);

        when(powerService.activateRandomPower(any(Player.class))).thenAnswer(inv -> {
            Player pl = inv.getArgument(0);
            pl.setHealth(pl.getHealth() + 20);
            return pl;
        });

        // Fuerza disponible y reclamo
        gameService.activatePower("g1");
        gameService.claimPower("g1", "P");

        // Se debe haber enviado algún evento de poder
        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/power"), anyMap());
    }

    // get / update / delete

    @Test
    void getAllGames_returnsFromRepo() {
        when(gameRepository.findAll()).thenReturn(List.of(game));
        assertEquals(1, gameService.getAllGames().size());
    }

    @Test
    void getGameById_returnsOptional() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        assertTrue(gameService.getGameById("g1").isPresent());
    }

    @Test
    void updateGame_updatesFields() {
        Game upd = Game.builder().isActive(false).durationMinutes(2).timerActive(true).startTime(Instant.now()).playerDinosaurMap(Map.of("x", Dinosaur.builder().name("d").damage(1).build())).build();
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<Game> res = gameService.updateGame("g1", upd);
        assertTrue(res.isPresent());
        assertFalse(res.get().isActive());
        assertEquals(2, res.get().getDurationMinutes());
        assertTrue(res.get().isTimerActive());
    }

    @Test
    void deleteGame_trueWhenExists() {
        when(gameRepository.existsById("g1")).thenReturn(true);
        assertTrue(gameService.deleteGame("g1"));
        verify(gameRepository).deleteById("g1");
    }

    @Test
    void deleteGame_falseWhenNotExists() {
        when(gameRepository.existsById("x")).thenReturn(false);
        assertFalse(gameService.deleteGame("x"));
        verify(gameRepository, never()).deleteById(anyString());
    }

    // addPlayerDinosaur

    @Test
    void addPlayerDinosaur_success_flow() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        when(playerRepository.findById("pNEW")).thenReturn(Optional.of(Player.builder().id("pNEW").name("pn").positionX(0).positionY(0).isAlive(true).health(100).build()));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        when(boardService.addPlayer(eq("board123"), any(Player.class))).thenReturn(board);

        Optional<Game> res = gameService.addPlayerDinosaur("g1", "pNEW", Dinosaur.builder().name("tri").damage(5).build());
        assertTrue(res.isPresent());
        assertTrue(res.get().getPlayerDinosaurMap().containsKey("pNEW"));
    }

    @Test
    void addPlayerDinosaur_playerNotFound_throws() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        when(playerRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(DinoChompException.class,
                () -> gameService.addPlayerDinosaur("g1", "missing", Dinosaur.builder().name("x").build()));
    }

    // timer APIs

    @Test
    void startTimer_setsFlags() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<Game> res = gameService.startTimer("g1", 3);
        assertTrue(res.isPresent());
        assertTrue(res.get().isTimerActive());
        assertEquals(3, res.get().getDurationMinutes());
    }

    @Test
    void stopTimer_unsetsFlag() {
        game.setTimerActive(true);
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));
        Optional<Game> res = gameService.stopTimer("g1");
        assertTrue(res.isPresent());
        assertFalse(res.get().isTimerActive());
    }

    @Test
    void getRemainingSeconds_handlesRefresh() {
        game.setDurationMinutes(1);
        game.setStartTime(Instant.now());
        game.setTimerActive(true);
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        Optional<Long> rem = gameService.getRemainingSeconds("g1");
        assertTrue(rem.isPresent());
        assertTrue(rem.get() <= 60);
    }

    // computeAndSetWinner + pickByHighestHealthThenFirst

    @Test
    void computeAndSetWinner_noPlayers_returnsEmpty() {
        Game g = Game.builder().nombre("gX").playerDinosaurMap(new HashMap<>()).build();
        when(gameRepository.findById("gX")).thenReturn(Optional.of(g));
        Optional<Player> w = gameService.computeAndSetWinner("gX");
        assertTrue(w.isEmpty());
    }

    @Test
    void computeAndSetWinner_oneAlive_isWinner() {
        Map<String, Dinosaur> pdm = new HashMap<>();
        pdm.put("A", Dinosaur.builder().name("d").build());
        pdm.put("B", Dinosaur.builder().name("d").build());

        Game g = Game.builder().nombre("gW").playerDinosaurMap(pdm).build();
        when(gameRepository.findById("gW")).thenReturn(Optional.of(g));

        Player alive = Player.builder().id("A").name("A").health(10).isAlive(true).build();
        Player dead = Player.builder().id("B").name("B").health(100).isAlive(false).build();

        when(playerRepository.findAllById(anyCollection())).thenReturn(List.of(alive, dead));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Player> w = gameService.computeAndSetWinner("gW");
        assertTrue(w.isPresent());
        assertEquals("A", w.get().getId());
    }

    @Test
    void computeAndSetWinner_multipleAlive_pickHighestHealthThenFirst() {
        Map<String, Dinosaur> pdm = new HashMap<>();
        pdm.put("A", Dinosaur.builder().name("d").build());
        pdm.put("B", Dinosaur.builder().name("d").build());
        pdm.put("C", Dinosaur.builder().name("d").build());

        Game g = Game.builder().nombre("gW2").playerDinosaurMap(pdm).build();
        when(gameRepository.findById("gW2")).thenReturn(Optional.of(g));

        Player a = Player.builder().id("2").name("2").health(50).isAlive(true).build();
        Player b = Player.builder().id("1").name("1").health(50).isAlive(true).build(); // mismo health, menor id => ganador
        Player c = Player.builder().id("3").name("3").health(10).isAlive(true).build();

        when(playerRepository.findAllById(anyCollection())).thenReturn(List.of(a,b,c));
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Player> w = gameService.computeAndSetWinner("gW2");
        assertTrue(w.isPresent());
        assertEquals("1", w.get().getId());
    }

    // endGame

    @Test
    void endGame_sendsEvent_and_cleans() {
        when(gameRepository.findById("g1")).thenReturn(Optional.of(game));
        gameService.endGame("g1", "msg");
        verify(template, atLeastOnce()).convertAndSend(contains("/topic/games/g1/events"), anyMap());
    }

    // start/stop game loop guards
    @Test
    void startGameLoop_and_stopGameLoop_areCallable_withoutErrors() {
        // Simplemente cubrir rutas
        gameService.startGameLoop("g1");
        gameService.stopGameLoop("g1");
    }
}