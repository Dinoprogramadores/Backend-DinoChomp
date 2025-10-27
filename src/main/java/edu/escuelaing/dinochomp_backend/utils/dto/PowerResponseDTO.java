package edu.escuelaing.dinochomp_backend.utils.dto;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PowerResponseDTO implements Serializable {
    private String name;
    private String type;
    private int duration; // en segundos
    
}
