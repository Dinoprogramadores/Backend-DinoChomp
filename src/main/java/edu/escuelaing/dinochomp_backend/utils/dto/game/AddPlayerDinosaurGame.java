package edu.escuelaing.dinochomp_backend.utils.dto.game;

import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddPlayerDinosaurGame {
    String gameId;
    String playerId;
    String dinosaurName;
}
