package edu.escuelaing.dinochomp_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Dinosaur")
public class Dinosaur {
    @Id
    private String id;
    private String species;
    private int size;
}
