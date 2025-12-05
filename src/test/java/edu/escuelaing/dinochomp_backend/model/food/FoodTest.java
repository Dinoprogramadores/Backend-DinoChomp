package edu.escuelaing.dinochomp_backend.model.food;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FoodTest {

    @Test
    void testFoodBuilder() {
        Food food = Food.builder()
                .id("1")
                .name("Apple")
                .positionX(5)
                .positionY(5)
                .nutritionValue(10)
                .build();

        assertEquals("1", food.getId());
        assertEquals("Apple", food.getName());
        assertEquals(5, food.getPositionX());
        assertEquals(5, food.getPositionY());
        assertEquals(10, food.getNutritionValue());
    }

    @Test
    void testFoodSetters() {
        Food food = new Food();
        food.setId("2");
        food.setName("Banana");
        food.setPositionX(10);
        food.setPositionY(10);
        food.setNutritionValue(15);

        assertEquals("2", food.getId());
        assertEquals("Banana", food.getName());
        assertEquals(10, food.getPositionX());
        assertEquals(10, food.getPositionY());
        assertEquals(15, food.getNutritionValue());
    }
}

