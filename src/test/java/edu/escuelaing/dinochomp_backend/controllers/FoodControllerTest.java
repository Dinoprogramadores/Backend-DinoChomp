package edu.escuelaing.dinochomp_backend.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.services.FoodService;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.mappers.FoodMapper;
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

class FoodControllerTest {

    private MockMvc mockMvc;

    @Mock
    private FoodService foodService;

    @Mock
    private FoodMapper foodMapper;

    @InjectMocks
    private FoodController foodController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private Food food;
    private FoodRequestDTO foodRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(foodController).build();
        food = Food.builder().id("1").name("Apple").nutritionValue(10).build();
        FoodResponseDTO foodResponseDTO = FoodResponseDTO.builder().id("1").name("Apple").nutrition(10).build();
        foodRequestDTO = FoodRequestDTO.builder().id("1").name("Apple").nutrition(10).build();

        when(foodMapper.toEntity(any(FoodRequestDTO.class))).thenReturn(food);
        when(foodMapper.toDTO(any(Food.class))).thenReturn(foodResponseDTO);
    }

    @Test
    void testCreateFood() throws Exception {
        when(foodService.createFood(any(Food.class))).thenReturn(food);

        mockMvc.perform(post("/foods")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foodRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void testGetAllFoods() throws Exception {
        when(foodService.getAllFoods()).thenReturn(Collections.singletonList(food));

        mockMvc.perform(get("/foods"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Apple"));
    }

    @Test
    void testGetFoodById() throws Exception {
        when(foodService.getFoodById("1")).thenReturn(Optional.of(food));

        mockMvc.perform(get("/foods/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void testGetFoodById_NotFound() throws Exception {
        when(foodService.getFoodById("2")).thenReturn(Optional.empty());

        mockMvc.perform(get("/foods/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateFood() throws Exception {
        when(foodService.updateFood(any(String.class), any(Food.class))).thenReturn(Optional.of(food));

        mockMvc.perform(put("/foods/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foodRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Apple"));
    }

    @Test
    void testUpdateFood_NotFound() throws Exception {
        when(foodService.updateFood(any(String.class), any(Food.class))).thenReturn(Optional.empty());

        mockMvc.perform(put("/foods/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(foodRequestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteFood() throws Exception {
        when(foodService.deleteFood("1")).thenReturn(true);

        mockMvc.perform(delete("/foods/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteFood_NotFound() throws Exception {
        when(foodService.deleteFood("2")).thenReturn(false);

        mockMvc.perform(delete("/foods/2"))
                .andExpect(status().isNotFound());
    }
}

