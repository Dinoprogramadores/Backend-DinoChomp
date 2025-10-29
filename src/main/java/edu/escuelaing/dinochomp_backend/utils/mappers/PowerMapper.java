package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerResponseDTO;

import org.springframework.stereotype.Component;

@Component
public class PowerMapper {

    public Power toEntity(PowerRequestDTO dto) {
        if (dto == null) return null;

        Power power;
        switch (dto.getType().toLowerCase()) {
            case "health":
                power = new HealthPower();
                break;
            default:
                throw new IllegalArgumentException("Tipo de poder desconocido: " + dto.getType());
        }
        // Set atributos comunes del padre
        power.setType(dto.getType());
        power.setDuration(dto.getDuration());
        power.setName(dto.getName());
        return power;
    }

    public PowerResponseDTO toDTO(Power p) {
        if (p == null) return null;
        return PowerResponseDTO.builder()
                .type(p.getType())
                .name(p.getName())
                .duration(p.getDuration())
                .build();
    }
}