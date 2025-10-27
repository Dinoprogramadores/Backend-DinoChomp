package edu.escuelaing.dinochomp_backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Food")
public class Food {
    @Id
    private String id;
    private String name;
    private int positionX;
    private int positionY;
    private int nutritionValue; // cantidad de vida/puntos que otorga al comer
}
