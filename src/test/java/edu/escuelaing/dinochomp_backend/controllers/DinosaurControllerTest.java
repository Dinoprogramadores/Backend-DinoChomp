package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.services.DinosaurService;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.DinosaurMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DinosaurControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DinosaurService dinosaurService;

    @Mock
    private DinosaurMapper dinosaurMapper;

    @InjectMocks
    private DinosaurController dinosaurController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Dinosaur dinosaur;
    private DinosaurRequestDTO dinosaurRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(dinosaurController).build();
        dinosaur = Dinosaur.builder().id("1").name("T-Rex").damage(100).build();
        DinosaurResponseDTO dinosaurResponseDTO = DinosaurResponseDTO.builder().id("1").name("T-Rex").damage(100).build();
        dinosaurRequestDTO = DinosaurRequestDTO.builder().id("1").name("T-Rex").damage(100).build();

        when(dinosaurMapper.toEntity(any(DinosaurRequestDTO.class))).thenReturn(dinosaur);
        when(dinosaurMapper.toDTO(any(Dinosaur.class))).thenReturn(dinosaurResponseDTO);
    }

    @Test
    void testCreateDinosaur() throws Exception {
        when(dinosaurService.createDinosaur(any(Dinosaur.class))).thenReturn(dinosaur);

        mockMvc.perform(post("/dinosaurs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dinosaurRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("T-Rex"));
    }

    @Test
    void testGetAllDinosaurs() throws Exception {
        when(dinosaurService.getAllDinosaurs()).thenReturn(Collections.singletonList(dinosaur));

        mockMvc.perform(get("/dinosaurs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("T-Rex"));
    }

    @Test
    void testGetDinosaurById() throws Exception {
        when(dinosaurService.getDinosaurById("1")).thenReturn(Optional.of(dinosaur));

        mockMvc.perform(get("/dinosaurs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("T-Rex"));
    }

    @Test
    void testGetDinosaurById_NotFound() throws Exception {
        when(dinosaurService.getDinosaurById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/dinosaurs/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateDinosaur() throws Exception {
        when(dinosaurService.updateDinosaur(any(String.class), any(Dinosaur.class))).thenReturn(Optional.of(dinosaur));

        mockMvc.perform(put("/dinosaurs/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dinosaurRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("T-Rex"));
    }

    @Test
    void testUpdateDinosaur_NotFound() throws Exception {
        when(dinosaurService.updateDinosaur(any(String.class), any(Dinosaur.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/dinosaurs/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dinosaurRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteDinosaur() throws Exception {
        when(dinosaurService.deleteDinosaur("1")).thenReturn(true);

        mockMvc.perform(delete("/dinosaurs/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteDinosaur_NotFound() throws Exception {
        when(dinosaurService.deleteDinosaur("2")).thenReturn(false);

        mockMvc.perform(delete("/dinosaurs/2"))
                .andExpect(status().isNotFound());
    }
}

