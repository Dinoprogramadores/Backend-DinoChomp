package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MovePlayerDTO {
    private String playerId;
    private int newX;
    private int newY;
}
