package edu.escuelaing.dinochomp_backend.model.board;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import edu.escuelaing.dinochomp_backend.model.food.Food;
import edu.escuelaing.dinochomp_backend.model.game.Player;

import java.io.Serial;
import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
        defaultImpl = Player.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Player.class, name = "PLAYER"),
        @JsonSubTypes.Type(value = Food.class, name = "FOOD"),
})
public abstract class BoardItem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
