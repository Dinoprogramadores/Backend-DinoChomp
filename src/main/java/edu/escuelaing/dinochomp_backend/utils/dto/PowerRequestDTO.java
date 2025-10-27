package edu.escuelaing.dinochomp_backend.utils.dto;

import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PowerRequestDTO implements Serializable {
    private String name;
    private int duration; // en segundos
    private String type;  


}