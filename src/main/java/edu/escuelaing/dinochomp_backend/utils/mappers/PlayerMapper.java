package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Player;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.player.PlayerResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class PlayerMapper {

    public Player toEntity(PlayerRequestDTO dto) {
        if (dto == null) return null;
        return Player.builder()
                .id(dto.getId())
                .name(dto.getName())
                .positionX(dto.getPositionX())
                .positionY(dto.getPositionY())
                .health(dto.getHealth())
                .isAlive(dto.isAlive())
                .build();
    }

    public PlayerResponseDTO toDTO(Player p) {
        if (p == null) return null;
        return PlayerResponseDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .positionX(p.getPositionX())
                .positionY(p.getPositionY())
                .health(p.getHealth())
                .isAlive(p.isAlive())
                .build();
    }
}