package edu.escuelaing.dinochomp_backend.utils.dto.player;
import lombok.AllArgsConstructor;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerResponseDTO {
    private String id;
    private String name;
    private String email;
    private String password;
    private int positionX;
    private int positionY;
    private int health;
    private boolean isAlive;
}
