package edu.escuelaing.dinochomp_backend.model.game;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "Game")
public class Game {

    @Id
    private String nombre; // usado como id

    private boolean isActive; //si inicio o no la partida

    // Nota: aunque el requerimiento pide "HashMap con key Player", usar objetos como key en JSON/MongoDB provoca problemas.
    // Por eso la implementación interna usa key = playerId (String) -> Dinosaur. En los DTOs aceptamos PlayerDTO o playerId
    // y el mapper convierte adecuadamente.
    private Map<String, String> playerDinosaurMap = new HashMap<>();

    private Player winner; //ganador del juego

    private Set<Power> powers = new HashSet<>(); // poderes activos en la partida

    private Board board; // tablero del juego

    // Temporizador: duración en minutos, instante de inicio y flag
    private int durationMinutes = 0;
    private Instant startTime;
    private boolean timerActive = false;

    // Helper: añade un par playerId -> dinosaur, respetando máximo 4 entries
    public boolean addPlayerDinosaur(String playerId, String dinosaurName) {
        if (playerDinosaurMap == null) playerDinosaurMap = new HashMap<>();
        if (playerDinosaurMap.size() >= 4) return false;
        playerDinosaurMap.put(playerId, dinosaurName);
        return true;
    }

    // Helper: elimina por playerId si existe
    public boolean removePlayerDinosaur(String playerId) {
        if (playerDinosaurMap == null) return false;
        return playerDinosaurMap.remove(playerId) != null;
    }

    // Retorna segundos restantes o null si no aplica
    public Long getRemainingSeconds() {
        if (!timerActive || startTime == null) return null;
        Instant now = Instant.now();
        Duration elapsed = Duration.between(startTime, now);
        long totalSeconds = durationMinutes * 60L;
        long remaining = totalSeconds - elapsed.getSeconds();
        return Math.max(remaining, 0);
    }

    public void refreshTimerState() {
        Long rem = getRemainingSeconds();
        if (rem != null && rem <= 0) {
            timerActive = false;
        }
    }
}
