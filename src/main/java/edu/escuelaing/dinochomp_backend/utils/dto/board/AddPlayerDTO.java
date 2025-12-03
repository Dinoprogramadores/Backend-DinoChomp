package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddPlayerDTO {
    private String playerId;
    private int positionX;
    private int positionY;
}
