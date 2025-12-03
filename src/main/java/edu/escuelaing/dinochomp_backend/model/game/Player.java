package edu.escuelaing.dinochomp_backend.model.game;

import edu.escuelaing.dinochomp_backend.model.board.BoardItem;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "Player")
public class Player extends BoardItem {
    @Id
    private String id;
    private String name;
    private String email;
    private String password;
    private int positionX;
    private int positionY;
    private int health;
    private boolean isAlive;

}

