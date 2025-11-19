package edu.escuelaing.dinochomp_backend.utils.dto.power;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PowerActivationtDTO implements Serializable {
    private String gameId;
    private String playerId; 


}