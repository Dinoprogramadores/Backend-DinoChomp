package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.services.PowerService;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.PowerMapper;
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

class PowerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PowerService powerService;

    @Mock
    private PowerMapper powerMapper;

    @InjectMocks
    private PowerController powerController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Power power;
    private PowerRequestDTO powerRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(powerController).build();
        power = new HealthPower(20);
        PowerResponseDTO powerResponseDTO = new PowerResponseDTO("HEALTH", 20);
        powerRequestDTO = new PowerRequestDTO("HEALTH", 20);

        when(powerMapper.toEntity(any(PowerRequestDTO.class))).thenReturn(power);
        when(powerMapper.toDTO(any(Power.class))).thenReturn(powerResponseDTO);
    }

    @Test
    void testCreatePower() throws Exception {
        when(powerService.createPower(any(Power.class))).thenReturn(power);

        mockMvc.perform(post("/powers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(powerRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("HEALTH"))
                .andExpect(jsonPath("$.health").value(20));
    }

    @Test
    void testGetAllPowers() throws Exception {
        when(powerService.getAllPowers()).thenReturn(Collections.singletonList(power));

        mockMvc.perform(get("/powers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("HEALTH"));
    }

    @Test
    void testGetPowerById() throws Exception {
        when(powerService.getPowerByName("HEALTH")).thenReturn(Optional.of(power));

        mockMvc.perform(get("/powers/HEALTH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HEALTH"));
    }

    @Test
    void testGetPowerById_NotFound() throws Exception {
        when(powerService.getPowerByName("UNKNOWN")).thenReturn(Optional.empty());

        mockMvc.perform(get("/powers/UNKNOWN"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdatePower() throws Exception {
        when(powerService.updatePower(any(String.class), any(Power.class))).thenReturn(Optional.of(power));

        mockMvc.perform(put("/powers/HEALTH")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(powerRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("HEALTH"));
    }

    @Test
    void testUpdatePower_NotFound() throws Exception {
        when(powerService.updatePower(any(String.class), any(Power.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/powers/UNKNOWN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(powerRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeletePower() throws Exception {
        when(powerService.deletePower("HEALTH")).thenReturn(true);

        mockMvc.perform(delete("/powers/HEALTH"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeletePower_NotFound() throws Exception {
        when(powerService.deletePower("UNKNOWN")).thenReturn(false);

        mockMvc.perform(delete("/powers/UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}

