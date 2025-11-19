package edu.escuelaing.dinochomp_backend.model.power;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Power")
public abstract class Power {
    @Id
    protected String name;


    
    public abstract Player applyEffect(Player player);
}
