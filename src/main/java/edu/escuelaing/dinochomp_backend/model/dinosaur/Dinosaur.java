package edu.escuelaing.dinochomp_backend.model.dinosaur;

import org.springframework.data.mongodb.core.mapping.*;
import org.springframework.data.annotation.Id;
import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Dinosaur")
public class Dinosaur {
    @Id
    private String id;
    private String name;
    private int damage;
    
}
