package edu.escuelaing.dinochomp_backend.model.power;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Power")
public class HealthPower extends Power {
    private int addedHealth;

    @Override
    public Player applyEffect(Player player) {
        int newHealth = Math.min(player.getHealth() + addedHealth, 100);
        player.setHealth(newHealth);
        return player;
    }
    
    
}
