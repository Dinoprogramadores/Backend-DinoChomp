package edu.escuelaing.dinochomp_backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
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
public class Player {
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

}

