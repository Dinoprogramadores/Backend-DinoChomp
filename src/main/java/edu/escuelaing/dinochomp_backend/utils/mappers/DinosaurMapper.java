package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class DinosaurMapper {

    public Dinosaur toEntity(DinosaurRequestDTO dto) {
        if (dto == null) return null;
        return Dinosaur.builder()
                .id(dto.getId())
                .name(dto.getName())
                .damage(dto.getDamage())
                .build();
    }

    public DinosaurResponseDTO toDTO(Dinosaur d) {
        if (d == null) return null;
        return DinosaurResponseDTO.builder()
                .id(d.getId())
                .name(d.getName())
                .damage(d.getDamage())
                .build();
    }
}