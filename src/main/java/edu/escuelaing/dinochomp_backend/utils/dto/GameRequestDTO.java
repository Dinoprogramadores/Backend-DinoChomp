package edu.escuelaing.dinochomp_backend.utils.dto;

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
    private Map<String, DinosaurDTO> playerDinosaurMap;
    private Set<Power> powers;
    private Map<String, Object> metadata;
    private int durationMinutes;
}

