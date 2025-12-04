package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.repository.GameRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.awt.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GameServiceSeedFoodCornerTest {

    @Mock
    SimpMessagingTemplate template;
    @Mock
    GameRepository gameRepository;
    @Mock
    BoardService boardService;
    @Mock
    PowerService powerService;
    @InjectMocks
    GameService gameService;

    @Test
    void seedFood_skipsCorners_and_respects_isFree() {
        Board board = Board.builder()
                .id("b1")
                .width(5)
                .height(5)
                .map(new HashMap<>())
                .build();

        // Esquinas ocupadas simuladas con no-null
        board.getMap().put(new Point(0,0), Food.builder().id("f0").positionX(0).positionY(0).build());
        board.getMap().put(new Point(4,0), Food.builder().id("f1").positionX(4).positionY(0).build());
        board.getMap().put(new Point(0,4), Food.builder().id("f2").positionX(0).positionY(4).build());
        board.getMap().put(new Point(4,4), Food.builder().id("f3").positionX(4).positionY(4).build());

        when(boardService.createBoard(5,5)).thenReturn(board);
        when(boardService.getBoard("b1")).thenReturn(Optional.of(board));
        when(boardService.addFood(eq("b1"), any(Food.class))).thenAnswer(inv -> board);

        Game g = Game.builder().nombre("gSeed").width(5).height(5).build();

        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> {
            Game saved = inv.getArgument(0);
            saved.setBoardId("b1");
            return saved;
        });

        Game saved = gameService.createGame(g, 5);

        assertEquals("b1", saved.getBoardId());
        // No validamos posiciones exactas por aleatoriedad, pero se ha intentado agregar
        verify(boardService, atLeastOnce()).addFood(eq("b1"), any(Food.class));
    }

    @Test
    void seedFood_returnsEarly_whenSmallBoard_orZeroFood() {
        Board board = Board.builder().id("b2").width(2).height(2).map(new HashMap<>()).build();
        when(boardService.createBoard(2,2)).thenReturn(board);
        when(gameRepository.save(any(Game.class))).thenAnswer(inv -> {
            Game saved = inv.getArgument(0);
            saved.setBoardId("b2");
            return saved;
        });

        Game g = Game.builder().nombre("gSmall").width(2).height(2).build();
        gameService.createGame(g, 10); // no debería sembrar
        verify(boardService, never()).addFood(anyString(), any(Food.class));

        Board board3 = Board.builder().id("b3").width(5).height(5).map(new HashMap<>()).build();
        when(boardService.createBoard(5,5)).thenReturn(board3);
        Game g2 = Game.builder().nombre("gZero").width(5).height(5).build();
        gameService.createGame(g2, 0);
        verify(boardService, never()).addFood(eq("b3"), any(Food.class));
    }
}