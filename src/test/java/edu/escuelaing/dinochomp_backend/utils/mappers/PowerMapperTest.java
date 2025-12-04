package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.power.HealthPower;
import edu.escuelaing.dinochomp_backend.model.power.Power;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.power.PowerResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PowerMapperTest {

    private final PowerMapper mapper = new PowerMapper();

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_health_buildsHealthPower() {
        PowerRequestDTO dto = PowerRequestDTO.builder()
                .name("HEALTH")
                .health(15)
                .build();
        Power p = mapper.toEntity(dto);
        assertNotNull(p);
        assertInstanceOf(HealthPower.class, p);
        assertEquals(15, ((HealthPower) p).getAddedHealth());
    }

    @Test
    void toEntity_unknown_throws() {
        PowerRequestDTO dto = PowerRequestDTO.builder()
                .name("SPEED")
                .health(0)
                .build();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.toEntity(dto));
        assertTrue(ex.getMessage().contains("Tipo de poder desconocido"));
    }

    @Test
    void toDTO_null_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_healthPower_buildsResponse() {
        HealthPower hp = new HealthPower(20);
        PowerResponseDTO dto = mapper.toDTO(hp);
        assertNotNull(dto);
        assertEquals("HEALTH", dto.getName());
        assertEquals(20, dto.getHealth());
    }

    @Test
    void toDTO_unknownPower_throws() {
        Power p = mock(Power.class);
        when(p.getName()).thenReturn("UNKNOWN");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> mapper.toDTO(p));
        assertTrue(ex.getMessage().contains("Clase de poder desconocida"));
    }
}
