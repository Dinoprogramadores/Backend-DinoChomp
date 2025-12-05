package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.GameService;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import edu.escuelaing.dinochomp_backend.utils.mappers.GameMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class GameControllerTest {

    private MockMvc mockMvc;

    @Mock
    private GameService gameService;

    @Mock
    private GameMapper gameMapper;

    @InjectMocks
    private GameController gameController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Game game;
    private GameRequestDTO gameRequestDTO;
    private GameResponseDTO gameResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(gameController).build();

        game = Game.builder()
                .nombre("game1")
                .isActive(true)
                .width(20)
                .height(20)
                .durationMinutes(5)
                .playerDinosaurMap(Map.of("p1", new Dinosaur()))
                .build();
        gameRequestDTO = GameRequestDTO.builder()
                .nombre("test-game")
                .isActive(true)
                .width(20)
                .height(20)
                .durationMinutes(5)
                .totalFood(1)
                .playerDinosaurMap(Map.of("p1", new DinosaurRequestDTO()))
                .build();
        gameResponseDTO = new GameResponseDTO();
        gameResponseDTO.setNombre("test-game");
    }

    @Test
    void testCreateGame() throws Exception {
        when(gameMapper.toEntity(any(GameRequestDTO.class))).thenReturn(game);
        when(gameService.createGame(any(Game.class), eq(1))).thenReturn(game);
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testGetAllGames() throws Exception {
        when(gameService.getAllGames()).thenReturn(Collections.singletonList(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(get("/games"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("test-game"));
    }

    @Test
    void testGetGameById() throws Exception {
        when(gameService.getGameById("game1")).thenReturn(Optional.of(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(get("/games/game1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testGetGameById_NotFound() throws Exception {
        when(gameService.getGameById("game2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/games/game2"))
                .andExpect(status().isNotFound());
    }


    @Test
    void testDeleteGame() throws Exception {
        when(gameService.deleteGame("game1")).thenReturn(true);

        mockMvc.perform(delete("/games/game1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateGame_BadRequest() throws Exception {
        gameRequestDTO.setNombre(null);

        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetBoardIdByGame() throws Exception {
        game.setBoardId("board1");
        when(gameService.getGameById("game1")).thenReturn(Optional.of(game));

        mockMvc.perform(get("/games/game1/board"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value("board1"));
    }

    @Test
    void testGetBoardIdByGame_NoBoard() throws Exception {
        game.setBoardId(null);
        when(gameService.getGameById("game1")).thenReturn(Optional.of(game));

        mockMvc.perform(get("/games/game1/board"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBoardIdByGame_GameNotFound() throws Exception {
        when(gameService.getGameById("game2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/games/game2/board"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateGame() throws Exception {
        when(gameMapper.toEntity(any(GameRequestDTO.class))).thenReturn(game);
        when(gameService.updateGame(eq("game1"), any(Game.class))).thenReturn(Optional.of(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(put("/games/game1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testUpdateGame_NotFound() throws Exception {
        when(gameMapper.toEntity(any(GameRequestDTO.class))).thenReturn(game);
        when(gameService.updateGame(eq("game2"), any(Game.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/games/game2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gameRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteGame_NotFound() throws Exception {
        when(gameService.deleteGame("game2")).thenReturn(false);

        mockMvc.perform(delete("/games/game2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddPlayerDinosaur() throws Exception {
        DinosaurRequestDTO dinosaurRequestDTO = new DinosaurRequestDTO();
        Dinosaur dinosaur = new Dinosaur();

        when(gameMapper.toEntity(any(DinosaurRequestDTO.class))).thenReturn(dinosaur);
        when(gameService.addPlayerDinosaur(eq("game1"), eq("p1"), any(Dinosaur.class))).thenReturn(Optional.of(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(
                        post("/games/game1/players/p1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dinosaurRequestDTO))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testAddPlayerDinosaur_Conflict() throws Exception {
        DinosaurRequestDTO dinosaurRequestDTO = new DinosaurRequestDTO();
        Dinosaur dinosaur = new Dinosaur();

        when(gameMapper.toEntity(any(DinosaurRequestDTO.class))).thenReturn(dinosaur);
        when(gameService.addPlayerDinosaur(eq("game1"), eq("p1"), any(Dinosaur.class))).thenReturn(Optional.empty());

        mockMvc.perform(
                        post("/games/game1/players/p1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dinosaurRequestDTO))
                )
                .andExpect(status().isConflict());
    }

    @Test
    void testStartTimer() throws Exception {
        when(gameService.startTimer("game1", 5)).thenReturn(Optional.of(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(post("/games/game1/timer/start")
                        .param("minutes", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testStartTimer_NotFound() throws Exception {
        when(gameService.startTimer("game2", 5)).thenReturn(Optional.empty());

        mockMvc.perform(post("/games/game2/timer/start")
                        .param("minutes", "5"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testStopTimer() throws Exception {
        when(gameService.stopTimer("game1")).thenReturn(Optional.of(game));
        when(gameMapper.toDTO(any(Game.class))).thenReturn(gameResponseDTO);

        mockMvc.perform(post("/games/game1/timer/stop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("test-game"));
    }

    @Test
    void testStopTimer_NotFound() throws Exception {
        when(gameService.stopTimer("game2")).thenReturn(Optional.empty());

        mockMvc.perform(post("/games/game2/timer/stop"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetRemaining() throws Exception {
        when(gameService.getRemainingSeconds("game1")).thenReturn(Optional.of(300L));

        mockMvc.perform(get("/games/game1/timer/remaining"))
                .andExpect(status().isOk())
                .andExpect(content().string("300"));
    }

    @Test
    void testGetRemaining_NotFound() throws Exception {
        when(gameService.getRemainingSeconds("game2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/games/game2/timer/remaining"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetWinner() throws Exception {
        Player winner = new Player();
        winner.setName("player1");
        when(gameService.getWinner("game1")).thenReturn(Optional.of(winner));

        mockMvc.perform(get("/games/game1/winner"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("player1"));
    }

    @Test
    void testGetWinner_NotFound() throws Exception {
        when(gameService.getWinner("game2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/games/game2/winner"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testComputeWinner() throws Exception {
        Player winner = new Player();
        winner.setName("player1");
        when(gameService.computeAndSetWinner("game1")).thenReturn(Optional.of(winner));

        mockMvc.perform(get("/games/game1/winner/compute"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("player1"));
    }

    @Test
    void testComputeWinner_NotFound() throws Exception {
        when(gameService.computeAndSetWinner("game2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/games/game2/winner/compute"))
                .andExpect(status().isNotFound());
    }
}
