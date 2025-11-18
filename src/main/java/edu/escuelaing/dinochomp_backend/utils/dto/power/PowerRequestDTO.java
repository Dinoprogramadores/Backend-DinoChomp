package edu.escuelaing.dinochomp_backend.utils.dto.power;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PowerRequestDTO implements Serializable {
    private String name;
    private int health;
    


}