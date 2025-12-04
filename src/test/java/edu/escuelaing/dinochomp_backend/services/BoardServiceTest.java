package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;
import edu.escuelaing.dinochomp_backend.model.board.BoardItem;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import edu.escuelaing.dinochomp_backend.utils.DinoChompException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.awt.Point;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BoardServiceTest {

    @Mock private BoardRepository boardRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private FoodRepository foodRepository;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks private BoardService boardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    private BoardDocument doc(String id, int w, int h) {
        BoardDocument d = new BoardDocument();
        d.setId(id);
        d.setWidth(w);
        d.setHeight(h);
        return d;
    }

    @Test
    @DisplayName("createBoard inicializa todas las celdas en Redis con EMPTY")
    void testCreateBoard() {
        when(boardRepository.save(any())).thenAnswer(inv -> {
            // devolver documento con id para formar la key de Redis
            BoardDocument saved = new BoardDocument();
            saved.setId("board1");
            saved.setWidth(5);
            saved.setHeight(5);
            return saved;
        });

        Board result = boardService.createBoard(5, 5);

        assertNotNull(result);
        assertEquals(5, result.getWidth());

        // Se escriben 25 celdas en la key board:board1:cells con valor "EMPTY"
        verify(hashOperations, times(25))
                .put(eq("board:board1:cells"), any(), eq("EMPTY"));
        verify(boardRepository, times(1)).save(any(BoardDocument.class));
    }

    @Test
    @DisplayName("getBoard mapea null/EMPTY/FOOD:/PLAYER: desde Redis")
    void testGetBoard_MapParsingAllBranches() {
        // Doc base
        BoardDocument d = doc("board1", 2, 2);
        when(boardRepository.findById("board1")).thenReturn(Optional.of(d));

        // Redis entries: null, EMPTY, FOOD, PLAYER
        Map<Object, Object> entries = new HashMap<>();
        entries.put("0,0", null);               // rama null/EMPTY
        entries.put("0,1", "EMPTY");            // rama null/EMPTY
        entries.put("1,0", "FOOD:f1");          // rama FOOD
        entries.put("1,1", "PLAYER:p1");        // rama PLAYER
        when(hashOperations.entries("board:board1:cells")).thenReturn(entries);

        // Repos para resolver FOOD/PLAYER
        Food food = new Food();
        food.setId("f1");
        Player player = new Player();
        player.setId("p1");
        when(foodRepository.findById("f1")).thenReturn(Optional.of(food));
        when(playerRepository.findById("p1")).thenReturn(Optional.of(player));

        Optional<Board> obt = boardService.getBoard("board1");
        assertTrue(obt.isPresent());
        Board board = obt.get();

        Map<Point, BoardItem> map = board.getMap();
        assertEquals(4, map.size());
        assertNull(map.get(new Point(0,0)));
        assertNull(map.get(new Point(0,1)));
        assertSame(food, map.get(new Point(1,0)));
        assertSame(player, map.get(new Point(1,1)));
    }

    @Test
    @DisplayName("getBoard retorna Optional.empty cuando el board no existe")
    void testGetBoard_NotFound() {
        when(boardRepository.findById("missing")).thenReturn(Optional.empty());
        assertTrue(boardService.getBoard("missing").isEmpty());
        verify(hashOperations, never()).entries(anyString());
    }

    @Test
    @DisplayName("addPlayer escribe en Redis y retorna el board mapeado")
    void testAddPlayer_Success() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(2);
        p.setPositionY(3);

        when(boardRepository.findById("board1")).thenReturn(Optional.of(doc("board1", 5, 5)));
        // getBoard leerá de Redis este contenido
        Map<Object, Object> entries = Map.of("2,3", "PLAYER:p1");
        when(hashOperations.entries("board:board1:cells")).thenReturn(entries);
        when(playerRepository.findById("p1")).thenReturn(Optional.of(p));

        Board board = boardService.addPlayer("board1", p);

        assertNotNull(board);
        verify(playerRepository).save(p);
        verify(hashOperations).put(eq("board:board1:cells"), eq("2,3"), eq("PLAYER:p1"));
        assertTrue(board.getMap().containsKey(new Point(2,3)));
    }

    @Test
    @DisplayName("addPlayer lanza DinoChompException si el board no existe")
    void testAddPlayer_BoardNotFound_Throws() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(2);
        p.setPositionY(3);

        when(boardRepository.findById("board1")).thenReturn(Optional.empty());

        DinoChompException ex = assertThrows(DinoChompException.class,
                () -> boardService.addPlayer("board1", p));
        assertEquals("Board not found", ex.getMessage());

        // Guardó el jugador y escribió en Redis antes de fallar
        verify(playerRepository).save(p);
        verify(hashOperations).put(eq("board:board1:cells"), eq("2,3"), eq("PLAYER:p1"));
    }

    @Test
    @DisplayName("addFood escribe en Redis y retorna el board mapeado")
    void testAddFood_Success() {
        Food f = new Food();
        f.setId("f1");
        f.setPositionX(1);
        f.setPositionY(4);

        when(boardRepository.findById("board1")).thenReturn(Optional.of(doc("board1", 5, 5)));
        // getBoard leerá de Redis este contenido
        Map<Object, Object> entries = Map.of("1,4", "FOOD:f1");
        when(hashOperations.entries("board:board1:cells")).thenReturn(entries);
        when(foodRepository.findById("f1")).thenReturn(Optional.of(f));

        Board board = boardService.addFood("board1", f);

        assertNotNull(board);
        verify(foodRepository).save(f);
        verify(hashOperations).put(eq("board:board1:cells"), eq("1,4"), eq("FOOD:f1"));
        assertSame(f, board.getMap().get(new Point(1,4)));
    }

    @Test
    @DisplayName("addFood lanza DinoChompException si el board no existe")
    void testAddFood_BoardNotFound_Throws() {
        Food f = new Food();
        f.setId("f1");
        f.setPositionX(1);
        f.setPositionY(4);

        when(boardRepository.findById("board1")).thenReturn(Optional.empty());

        DinoChompException ex = assertThrows(DinoChompException.class,
                () -> boardService.addFood("board1", f));
        assertEquals("Board not found", ex.getMessage());

        verify(foodRepository).save(f);
        verify(hashOperations).put(eq("board:board1:cells"), eq("1,4"), eq("FOOD:f1"));
    }

    @Test
    @DisplayName("movePlayer retorna empty si el destino está ocupado por otro jugador (no escribe en Redis)")
    void testMovePlayer_BlockByPlayer() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(0);
        p.setPositionY(0);

        when(hashOperations.get(eq("board:board1:cells"), eq("1,1"))).thenReturn("PLAYER:other");

        Optional<Food> result = boardService.movePlayer("board1", p, 1, 1);

        assertTrue(result.isEmpty());
        verify(hashOperations, never()).put(anyString(), any(), any());
        verify(playerRepository, never()).save(any(Player.class));
        // Posición del jugador no cambia
        assertEquals(0, p.getPositionX());
        assertEquals(0, p.getPositionY());
    }

    @Test
    @DisplayName("movePlayer con comida en destino: la elimina, mueve y retorna la Food")
    void testMovePlayer_ReturnsFood() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(0);
        p.setPositionY(0);

        Food food = new Food();
        food.setId("f1");
        food.setNutritionValue(10);

        when(hashOperations.get(eq("board:board1:cells"), eq("1,1"))).thenReturn("FOOD:f1");
        when(foodRepository.findById("f1")).thenReturn(Optional.of(food));

        Optional<Food> result = boardService.movePlayer("board1", p, 1, 1);

        assertTrue(result.isPresent());
        assertEquals("f1", result.get().getId());
        verify(foodRepository).deleteById("f1");
        // Limpia origen y escribe destino
        verify(hashOperations).put(eq("board:board1:cells"), eq("0,0"), eq("EMPTY"));
        verify(hashOperations).put(eq("board:board1:cells"), eq("1,1"), eq("PLAYER:p1"));
        // Actualiza jugador
        verify(playerRepository).save(any(Player.class));
        assertEquals(1, p.getPositionX());
        assertEquals(1, p.getPositionY());
    }

    @Test
    @DisplayName("movePlayer destino vacío/null: mueve sin consumir comida (Optional.empty)")
    void testMovePlayer_EmptyDestinationMoves() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(2);
        p.setPositionY(2);

        // null simula que no hay valor en Redis para esa celda
        when(hashOperations.get(eq("board:board1:cells"), eq("3,2"))).thenReturn(null);

        Optional<Food> result = boardService.movePlayer("board1", p, 3, 2);

        assertTrue(result.isEmpty());
        verify(hashOperations).put(eq("board:board1:cells"), eq("2,2"), eq("EMPTY"));
        verify(hashOperations).put(eq("board:board1:cells"), eq("3,2"), eq("PLAYER:p1"));
        verify(playerRepository).save(p);
        assertEquals(3, p.getPositionX());
        assertEquals(2, p.getPositionY());
    }
}