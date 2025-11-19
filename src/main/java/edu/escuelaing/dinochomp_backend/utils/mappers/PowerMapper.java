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

        // El único poder actual es HEALTH
        if ("HEALTH".equalsIgnoreCase(dto.getName())) {
            return new HealthPower(dto.getHealth());
        }

        throw new IllegalArgumentException("Tipo de poder desconocido: " + dto.getName());
    }

    public PowerResponseDTO toDTO(Power p) {
        if (p == null) return null;

        if (p instanceof HealthPower hp) {
            return PowerResponseDTO.builder()
                    .name("HEALTH")
                    .health(hp.getAddedHealth())
                    .build();
        }

        throw new IllegalArgumentException("Clase de poder desconocida: " + p.getClass());
    }
}