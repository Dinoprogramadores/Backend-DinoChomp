package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.Data;

@Data
public class AddPlayerDTO {
    private String playerId;
    private int positionX;
    private int positionY;
}