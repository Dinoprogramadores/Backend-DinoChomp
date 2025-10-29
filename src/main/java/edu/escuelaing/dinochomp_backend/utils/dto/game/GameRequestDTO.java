package edu.escuelaing.dinochomp_backend.utils.dto.game;

import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameRequestDTO {
    private String nombre;
    private boolean isActive;
    // key = playerId (String) -> DinosaurDTO
    private Map<String, DinosaurRequestDTO> playerDinosaurMap;
    private Set<Power> powers;
    private int width;
    private int height;
    private int totalFood;
    private int durationMinutes;
}

