package edu.escuelaing.dinochomp_backend.model.power;

import edu.escuelaing.dinochomp_backend.model.game.Player;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Getter
@Setter
@Builder
@Document(collection = "Power")
public class HealthPower extends Power {
    private int addedHealth;

    public HealthPower(int addedHealth) {
        this.addedHealth = addedHealth;
        this.name = "HEALTH";
    }

    @Override
    public Player applyEffect(Player player) {
        System.out.println("estamos aplicando el poder de heal player al jugador: "+ player.getName());
        System.out.println("vida antes "+ player.getHealth());
        int newHealth = Math.min(player.getHealth() + addedHealth, 100);
        player.setHealth(newHealth);
        System.out.println("vida despues del math min: "+ player.getHealth());
        return player;
    }
    
    
}
