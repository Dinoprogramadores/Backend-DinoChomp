package edu.escuelaing.dinochomp_backend.utils.dto;

import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import lombok.*;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Clase que representa la respuesta del juego.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GameResponseDTO {
    /**
     * Nombre del juego.
     */
    private String nombre;
    /**
     * Indica si el juego está activo.
     */
    private boolean isActive;
    /**
     * Mapa de dinosaurios de los jugadores.
     */
    private Map<String, DinosaurDTO> playerDinosaurMap;
    /**
     * Conjunto de poderes disponibles en el juego.
     */
    private Set<Power> powers;
    /**
     * Metadatos adicionales del juego.
     */
    private Map<String, Object> metadata;
    /**
     * Duración del juego en minutos.
     */
    private int durationMinutes;
    /**
     * Hora de inicio del juego.
     */
    private Instant startTime;
    /**
     * Indica si el temporizador está activo.
     */
    private boolean timerActive;
    /**
     * Segundos restantes del juego.
     */
    private Long remainingSeconds;
}
