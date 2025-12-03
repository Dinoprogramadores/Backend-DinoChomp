package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.BoardService;
import edu.escuelaing.dinochomp_backend.services.PlayerService;
import edu.escuelaing.dinochomp_backend.utils.dto.board.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BoardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BoardService boardService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private BoardController controller;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void testCreateBoard() throws Exception {
        CreateBoardRequestDTO dto = new CreateBoardRequestDTO(10, 10);

        when(boardService.createBoard(10, 10))
                .thenReturn(new Board(10, 10));

        mockMvc.perform(post("/boards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBoard() throws Exception {
        Board board = new Board(5, 5);
        board.setId("x");

        when(boardService.getBoard("x")).thenReturn(Optional.of(board));

        mockMvc.perform(get("/boards/x"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetBoard_NotFound() throws Exception {
        when(boardService.getBoard("x")).thenReturn(Optional.empty());

        mockMvc.perform(get("/boards/x"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testMovePlayer() throws Exception {
        MovePlayerDTO dto = new MovePlayerDTO("p1", 2, 2);

        Player player = new Player();
        player.setId("p1");

        when(playerService.getPlayerById("p1")).thenReturn(Optional.of(player));
        when(boardService.movePlayer("b1", player, 2, 2)).thenReturn(Optional.of(new Food()));

        mockMvc.perform(post("/boards/b1/players/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }
}
