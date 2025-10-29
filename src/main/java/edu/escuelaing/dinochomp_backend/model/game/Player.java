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
    private String password;
    private int positionX;
    private int positionY;
    private int health;
    private boolean isAlive;

    

    public void move(String direction) {
        switch (direction) {
            case "UP" -> positionY -= 1;
            case "DOWN" -> positionY += 1;
            case "LEFT" -> positionX -= 1;
            case "RIGHT" -> positionX += 1;
        }
    }

    public void loseHealth(int amount) {
        if (isAlive) {
            health -= amount;
            if (health <= 0) {
                health = 0;
                isAlive = false;
            }
        }
    }

    public void addHealth(int amount) {
        if (isAlive) {
            health += amount;
        }
    }

}

