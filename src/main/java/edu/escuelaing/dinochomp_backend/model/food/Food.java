package edu.escuelaing.dinochomp_backend.model.food;

import edu.escuelaing.dinochomp_backend.model.board.BoardItem;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Food")
public class Food extends BoardItem {
    @Id
    private String id;
    private String name;
    private int positionX;
    private int positionY;
    private int nutritionValue; // cantidad de vida/puntos que otorga al comer
}
