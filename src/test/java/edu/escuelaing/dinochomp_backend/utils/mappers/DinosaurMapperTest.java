package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurResponseDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DinosaurMapperTest {

    private final DinosaurMapper mapper = new DinosaurMapper();

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_buildsDinosaur() {
        DinosaurRequestDTO req = DinosaurRequestDTO.builder()
                .id("d1")
                .name("Rex")
                .damage(7)
                .build();
        Dinosaur d = mapper.toEntity(req);
        assertEquals("d1", d.getId());
        assertEquals("Rex", d.getName());
        assertEquals(7, d.getDamage());
    }

    @Test
    void toDTO_null_returnsNull() {
        assertNull(mapper.toDTO(null));
    }

    @Test
    void toDTO_buildsResponse() {
        Dinosaur d = Dinosaur.builder()
                .id("d1")
                .name("Rex")
                .damage(7)
                .build();
        DinosaurResponseDTO dto = mapper.toDTO(d);
        assertEquals("d1", dto.getId());
        assertEquals("Rex", dto.getName());
        assertEquals(7, dto.getDamage());
    }
}

