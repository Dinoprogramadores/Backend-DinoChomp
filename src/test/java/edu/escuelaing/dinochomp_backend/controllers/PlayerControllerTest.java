package edu.escuelaing.dinochomp_backend.controllers;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.services.PlayerService;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.PlayerMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PlayerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PlayerService playerService;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerController playerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(playerController).build();
    }

    // POST /players
    @Test
    void testCreatePlayer() throws Exception {

        PlayerRequestDTO requestDTO = PlayerRequestDTO.builder()
                .name("John")
                .email("john@test.com")
                .password("1234")
                .positionX(10)
                .positionY(20)
                .health(100)
                .isAlive(true)
                .build();

        Player entity = Player.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .password("1234")
                .positionX(10)
                .positionY(20)
                .health(100)
                .isAlive(true)
                .build();

        PlayerResponseDTO responseDTO = PlayerResponseDTO.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .password("1234")
                .positionX(10)
                .positionY(20)
                .health(100)
                .isAlive(true)
                .build();

        when(playerMapper.toEntity(any(PlayerRequestDTO.class))).thenReturn(entity);
        when(playerService.savePlayer(any(Player.class))).thenReturn(entity);
        when(playerMapper.toDTO(any(Player.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                           "name": "John",
                           "email": "john@test.com",
                           "password": "1234",
                           "positionX": 10,
                           "positionY": 20,
                           "health": 100,
                           "alive": true
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    // GET /players/{id}
    @Test
    void testGetPlayerById() throws Exception {

        Player entity = Player.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .build();

        PlayerResponseDTO dto = PlayerResponseDTO.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .build();

        when(playerService.getPlayerById("1")).thenReturn(Optional.of(entity));
        when(playerMapper.toDTO(entity)).thenReturn(dto);

        mockMvc.perform(get("/players/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John"));
    }

    @Test
    void testGetPlayerById_NotFound() throws Exception {
        when(playerService.getPlayerById("99")).thenReturn(Optional.empty());

        mockMvc.perform(get("/players/99"))
                .andExpect(status().isNotFound());
    }

    // GET /players/email
    @Test
    void testGetPlayerByEmail() throws Exception {

        Player entity = Player.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .build();

        PlayerResponseDTO dto = PlayerResponseDTO.builder()
                .id("1")
                .name("John")
                .email("john@test.com")
                .build();

        when(playerService.getPlayerByEmail("john@test.com")).thenReturn(Optional.of(entity));
        when(playerMapper.toDTO(entity)).thenReturn(dto);

        mockMvc.perform(get("/players/email")
                        .param("email", "john@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@test.com"));
    }

    @Test
    void testGetPlayerByEmail_NotFound() throws Exception {
        when(playerService.getPlayerByEmail("none@test.com")).thenReturn(Optional.empty());

        mockMvc.perform(get("/players/email")
                        .param("email", "none@test.com"))
                .andExpect(status().isNotFound());
    }

    // GET /players
    @Test
    void testGetAllPlayers() throws Exception {

        Player p1 = Player.builder().id("1").name("A").email("a@test.com").build();
        Player p2 = Player.builder().id("2").name("B").email("b@test.com").build();

        PlayerResponseDTO dto1 = PlayerResponseDTO.builder()
                .id("1").name("A").email("a@test.com").build();
        PlayerResponseDTO dto2 = PlayerResponseDTO.builder()
                .id("2").name("B").email("b@test.com").build();

        when(playerService.getAllPlayers()).thenReturn(List.of(p1, p2));
        when(playerMapper.toDTO(p1)).thenReturn(dto1);
        when(playerMapper.toDTO(p2)).thenReturn(dto2);

        mockMvc.perform(get("/players"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("A"))
                .andExpect(jsonPath("$[1].name").value("B"));
    }

    // DELETE /players/{id}
    @Test
    void testDeletePlayer() throws Exception {
        when(playerService.deletePlayer("1")).thenReturn(true);

        mockMvc.perform(delete("/players/1"))
                .andExpect(status().isNoContent());

        verify(playerService, times(1)).deletePlayer("1");
    }

    @Test
    void testDeletePlayer_NotFound() throws Exception {
        when(playerService.deletePlayer("1")).thenReturn(false);

        mockMvc.perform(delete("/players/1"))
                .andExpect(status().isNotFound());
    }
}
