package edu.escuelaing.dinochomp_backend.utils.dto.player;

import java.io.Serializable;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerRequestDTO implements Serializable {
    private String id;
    private String name;
    private String password;
    private int positionX;
    private int positionY;
    private int health;
    private boolean isAlive;

    
}