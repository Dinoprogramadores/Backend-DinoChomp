package edu.escuelaing.dinochomp_backend.utils.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DinosaurResponseDTO {
    private String id;
    private String name;
    private int damage;
}
