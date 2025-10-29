package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GameMapper {

    private final DinosaurMapper dinosaurMapper = new DinosaurMapper();

    public Game toEntity(GameRequestDTO dto) {
        if (dto == null) return null;
        Map<String, String> dinos = null;
        if (dto.getPlayerDinosaurMap() != null) {
            dinos = dto.getPlayerDinosaurMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue));
        }
        Board board = new Board(dto.getWidth(), dto.getHeight());
        return Game.builder()
                .nombre(dto.getNombre())
                .isActive(dto.isActive())
                .playerDinosaurMap(dinos)
                .powers(dto.getPowers())
                .board(board)
                .durationMinutes(dto.getDurationMinutes())
                .build();
    }

    public GameResponseDTO toDTO(Game g) {
        if (g == null) return null;
        g.refreshTimerState();
        Map<String, String> dinos = null;
        if (g.getPlayerDinosaurMap() != null) {
            dinos = g.getPlayerDinosaurMap().entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue));
        }
        return GameResponseDTO.builder()
                .nombre(g.getNombre())
                .isActive(g.isActive())
                .playerDinosaurMap(dinos)
                .powers(g.getPowers())
                .boardId(g.getBoard().getId())
                .durationMinutes(g.getDurationMinutes())
                .startTime(g.getStartTime())
                .timerActive(g.isTimerActive())
                .remainingSeconds(g.getRemainingSeconds())
                .build();
    }
}

