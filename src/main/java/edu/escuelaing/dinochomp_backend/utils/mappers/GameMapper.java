package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    public Game toEntity(GameRequestDTO dto) {
        if (dto == null) return null;
        Map<String, Dinosaur> dinos = null;
        if (dto.getPlayerDinosaurMap() != null) {
            dinos = dto.getPlayerDinosaurMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> toEntity(e.getValue())));
        }
        return Game.builder()
                .nombre(dto.getNombre())
                .width(dto.getWidth())
                .height(dto.getHeight())
                .isActive(dto.isActive())
                .playerDinosaurMap(dinos)
                .powers(dto.getPowers())
                .durationMinutes(dto.getDurationMinutes())
                .build();
    }

    public GameResponseDTO toDTO(Game g) {
        if (g == null) return null;
        g.refreshTimerState();
        Map<String, DinosaurRequestDTO> dinos = null;
        if (g.getPlayerDinosaurMap() != null) {
            dinos = g.getPlayerDinosaurMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> toDTO(e.getValue())));
        }
        return GameResponseDTO.builder()
                .nombre(g.getNombre())
                .boardId(g.getBoardId())
                .isActive(g.isActive())
                .playerDinosaurMap(dinos)
                .powers(g.getPowers())
                .durationMinutes(g.getDurationMinutes())
                .startTime(g.getStartTime())
                .timerActive(g.isTimerActive())
                .remainingSeconds(g.getRemainingSeconds())
                .build();
    }

    public Dinosaur toEntity(DinosaurRequestDTO dto) {
        if (dto == null) return null;
        return Dinosaur.builder()
                .id(dto.getId())
                .name(dto.getName())
                .damage(dto.getDamage())
                .build();
    }

    public DinosaurRequestDTO toDTO(Dinosaur d) {
        if (d == null) return null;
        return DinosaurRequestDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .damage(d.getDamage())
                .build();
    }
}

