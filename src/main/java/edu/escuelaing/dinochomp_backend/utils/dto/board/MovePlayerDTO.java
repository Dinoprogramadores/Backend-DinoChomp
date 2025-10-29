package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.Data;

@Data
public class MovePlayerDTO {
    private String playerId;
    private int newX;
    private int newY;
}