package edu.escuelaing.dinochomp_backend.services;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.repository.FoodRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FoodServiceTest {

    @Mock
    private FoodRepository foodRepository;

    @InjectMocks
    private FoodService foodService;

    private Food food;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        food = Food.builder()
                .id("1")
                .name("Apple")
                .positionX(5)
                .positionY(5)
                .nutritionValue(10)
                .build();
    }

    @Test
    void testGetAllFoods() {
        when(foodRepository.findAll()).thenReturn(Collections.singletonList(food));
        List<Food> foods = foodService.getAllFoods();
        assertFalse(foods.isEmpty());
        assertEquals(1, foods.size());
        verify(foodRepository, times(1)).findAll();
    }

    @Test
    void testGetFoodById() {
        when(foodRepository.findById("1")).thenReturn(Optional.of(food));
        Optional<Food> foundFood = foodService.getFoodById("1");
        assertTrue(foundFood.isPresent());
        assertEquals("1", foundFood.get().getId());
        verify(foodRepository, times(1)).findById("1");
    }

    @Test
    void testGetFoodById_NotFound() {
        when(foodRepository.findById("2")).thenReturn(Optional.empty());
        Optional<Food> foundFood = foodService.getFoodById("2");
        assertFalse(foundFood.isPresent());
        verify(foodRepository, times(1)).findById("2");
    }

    @Test
    void testCreateFood() {
        when(foodRepository.save(any(Food.class))).thenReturn(food);
        Food createdFood = foodService.createFood(food);
        assertNotNull(createdFood);
        assertEquals("Apple", createdFood.getName());
        verify(foodRepository, times(1)).save(food);
    }

    @Test
    void testUpdateFood() {
        Food updatedDetails = Food.builder().name("Golden Apple").nutritionValue(20).build();
        when(foodRepository.findById("1")).thenReturn(Optional.of(food));
        when(foodRepository.save(any(Food.class))).thenReturn(food);

        Optional<Food> updatedFood = foodService.updateFood("1", updatedDetails);

        assertTrue(updatedFood.isPresent());
        verify(foodRepository, times(1)).findById("1");
        verify(foodRepository, times(1)).save(food);
        assertEquals("Golden Apple", food.getName());
        assertEquals(20, food.getNutritionValue());
    }

    @Test
    void testUpdateFood_NotFound() {
        Food updatedDetails = Food.builder().build();
        when(foodRepository.findById("2")).thenReturn(Optional.empty());
        Optional<Food> updatedFood = foodService.updateFood("2", updatedDetails);
        assertFalse(updatedFood.isPresent());
        verify(foodRepository, times(1)).findById("2");
        verify(foodRepository, never()).save(any(Food.class));
    }

    @Test
    void testDeleteFood() {
        when(foodRepository.existsById("1")).thenReturn(true);
        doNothing().when(foodRepository).deleteById("1");
        boolean deleted = foodService.deleteFood("1");
        assertTrue(deleted);
        verify(foodRepository, times(1)).existsById("1");
        verify(foodRepository, times(1)).deleteById("1");
    }

    @Test
    void testDeleteFood_NotFound() {
        when(foodRepository.existsById("2")).thenReturn(false);
        boolean deleted = foodService.deleteFood("2");
        assertFalse(deleted);
        verify(foodRepository, times(1)).existsById("2");
        verify(foodRepository, never()).deleteById(anyString());
    }
}

