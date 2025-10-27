package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.Food;
import edu.escuelaing.dinochomp_backend.utils.dto.FoodRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.FoodResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class FoodMapper {

    public Food toEntity(FoodRequestDTO dto) {
        if (dto == null) return null;
        return Food.builder()
                .id(dto.getId())
                .positionX(dto.getPositionX())
                .positionY(dto.getPositionY())
                .nutritionValue(dto.getNutrition())
                .build();
    }

    public FoodResponseDTO toDTO(Food f) {
        if (f == null) return null;
        return FoodResponseDTO.builder()
                .id(f.getId())
                .positionX(f.getPositionX())
                .positionY(f.getPositionY())
                .nutrition(f.getNutritionValue())
                .build();
    }
}
