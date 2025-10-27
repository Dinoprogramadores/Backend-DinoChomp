package edu.escuelaing.dinochomp_backend.model;

import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.persistence.Id;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Power")
public abstract class Power {
    @Id
    protected String name;
    protected int duration; // en segundos
    private String type; //health, speed, shield, etc.

    
    public abstract void applyEffect(Player player);
}
