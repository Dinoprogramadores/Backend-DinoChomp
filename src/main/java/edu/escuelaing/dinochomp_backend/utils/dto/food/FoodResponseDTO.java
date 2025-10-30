package edu.escuelaing.dinochomp_backend.utils.dto.food;
import java.io.Serializable;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FoodResponseDTO implements Serializable{
    private String id;
    private String name;
    private int positionX;
    private int positionY;
    private int nutrition; // cantidad de vida/puntos que otorga al comer
}
