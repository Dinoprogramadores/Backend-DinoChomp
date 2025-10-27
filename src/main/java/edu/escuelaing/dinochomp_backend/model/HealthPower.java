package edu.escuelaing.dinochomp_backend.model;

import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Dinosaur")
public class HealthPower extends Power{
    private int addedHealth;

    @Override
    public void applyEffect(Player player) {
        int newHealth = Math.min(player.getHealth() + addedHealth, player.getHealth());
        player.setHealth(newHealth);
    }
    
    
}
