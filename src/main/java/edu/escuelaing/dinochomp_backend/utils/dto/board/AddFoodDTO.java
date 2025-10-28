package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.Data;

@Data
public class AddFoodDTO {
    private String foodId;
    private int positionX;
    private int positionY;
    private int nutritionValue;
}