package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.food.FoodResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodMapperTest {

    private final FoodMapper mapper = new FoodMapper();

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_buildsFood() {
        FoodRequestDTO req = FoodRequestDTO.builder()
                .id("f1")
                .positionX(3)
                .positionY(4)
                .nutrition(12)
                .build();
        Food f = mapper.toEntity(req);
        assertEquals("f1", f.getId());
        assertEquals(3, f.getPositionX());
        assertEquals(4, f.getPositionY());
        assertEquals(12, f.getNutritionValue());
    }

    @Test
    void toDTO_null_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_buildsResponse() {
        Food f = Food.builder()
                .id("f1")
                .positionX(3)
                .positionY(4)
                .nutritionValue(12)
                .build();
        FoodResponseDTO dto = mapper.toDTO(f);
        assertEquals("f1", dto.getId());
        assertEquals(3, dto.getPositionX());
        assertEquals(4, dto.getPositionY());
        assertEquals(12, dto.getNutrition());
    }
}

