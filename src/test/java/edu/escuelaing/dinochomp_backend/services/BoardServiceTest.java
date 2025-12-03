package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.repository.BoardRepository;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import edu.escuelaing.dinochomp_backend.repository.PlayerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BoardServiceTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private FoodRepository foodRepository;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    void testCreateBoard() {
        BoardDocument document = new BoardDocument();
        document.setId("board1");
        document.setWidth(5);
        document.setHeight(5);

        when(boardRepository.save(any())).thenReturn(document);

        Board result = boardService.createBoard(5, 5);

        assertNotNull(result);
        assertEquals(5, result.getWidth());
        verify(boardRepository, times(1)).save(any());
        verify(hashOperations, atLeastOnce()).put(any(), any(), any());
    }

    @Test
    void testAddPlayer() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(2);
        p.setPositionY(3);

        BoardDocument doc = new BoardDocument();
        doc.setId("board1");
        doc.setWidth(5);
        doc.setHeight(5);

        when(boardRepository.findById("board1")).thenReturn(Optional.of(doc));
        when(foodRepository.findById(any())).thenReturn(Optional.empty());
        when(playerRepository.findById(any())).thenReturn(Optional.of(p));

        boardService.addPlayer("board1", p);

        verify(playerRepository).save(p);
        verify(hashOperations).put(any(), eq("2,3"), contains("PLAYER:"));
    }

    @Test
    void testMovePlayer_ReturnsFood() {
        Player p = new Player();
        p.setId("p1");
        p.setPositionX(0);
        p.setPositionY(0);

        Food food = new Food();
        food.setId("f1");
        food.setNutritionValue(10);

        when(hashOperations.get(any(), eq("1,1"))).thenReturn("FOOD:f1");
        when(foodRepository.findById("f1")).thenReturn(Optional.of(food));

        Optional<Food> result = boardService.movePlayer("board1", p, 1, 1);

        assertTrue(result.isPresent());
        assertEquals("f1", result.get().getId());
        verify(foodRepository).deleteById("f1");
        verify(playerRepository).save(any(Player.class));
    }
}
