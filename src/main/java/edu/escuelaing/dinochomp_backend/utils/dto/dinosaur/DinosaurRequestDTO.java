package edu.escuelaing.dinochomp_backend.utils.dto.dinosaur;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DinosaurRequestDTO {
    private String id;
    private String name;
    private int damage;
    
}
