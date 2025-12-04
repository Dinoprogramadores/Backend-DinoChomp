package edu.escuelaing.dinochomp_backend.utils.dto.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddFoodDTOTest {

    @Test
    void builder_setsAllFields() {
        AddFoodDTO dto = AddFoodDTO.builder()
                .foodId("f1")
                .positionX(1)
                .positionY(2)
                .nutritionValue(10)
                .build();
        assertEquals("f1", dto.getFoodId());
        assertEquals(1, dto.getPositionX());
        assertEquals(2, dto.getPositionY());
        assertEquals(10, dto.getNutritionValue());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        AddFoodDTO dto = new AddFoodDTO();
        dto.setFoodId("f2");
        dto.setPositionX(3);
        dto.setPositionY(4);
        dto.setNutritionValue(5);
        assertEquals("f2", dto.getFoodId());
        assertEquals(3, dto.getPositionX());
        assertEquals(4, dto.getPositionY());
        assertEquals(5, dto.getNutritionValue());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        AddFoodDTO dto = new AddFoodDTO("f3",7,8,9);
        assertEquals("f3", dto.getFoodId());
        assertEquals(7, dto.getPositionX());
        assertEquals(8, dto.getPositionY());
        assertEquals(9, dto.getNutritionValue());
    }
}

