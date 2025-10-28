package edu.escuelaing.dinochomp_backend.utils.dto;
import java.io.Serializable;
import lombok.Getter;
import lombok.*;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// DTO for player move messages from the front client
public class PlayerMoveMessage implements Serializable {
    private String playerId;
    private String direction; // "UP","DOWN","LEFT","RIGHT" or custom

}