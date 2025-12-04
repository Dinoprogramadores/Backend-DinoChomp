package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.dinosaur.Dinosaur;
import edu.escuelaing.dinochomp_backend.model.game.Game;
import edu.escuelaing.dinochomp_backend.utils.dto.dinosaur.DinosaurRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameRequestDTO;
import edu.escuelaing.dinochomp_backend.utils.dto.game.GameResponseDTO;
import edu.escuelaing.dinochomp_backend.utils.enums.Power;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GameMapperTest {

    private final GameMapper mapper = new GameMapper();

    @Test
    void toEntity_null_returnsNull() {
        assertNull(mapper.toEntity((GameRequestDTO) null));
    }

    @Test
    void toEntity_buildsGameWithDinos() {
        DinosaurRequestDTO d1 = DinosaurRequestDTO.builder().id("d1").name("Rex").damage(5).build();
        Map<String, DinosaurRequestDTO> playerDinoMap = Map.of("p1", d1);
        GameRequestDTO req = GameRequestDTO.builder()
                .nombre("Partida")
                .width(10)
                .height(20)
                .isActive(true)
                .playerDinosaurMap(playerDinoMap)
                .powers(Set.of(Power.HEALING))
                .durationMinutes(15)
                .build();
        Game game = mapper.toEntity(req);
        assertEquals("Partida", game.getNombre());
        assertEquals(10, game.getWidth());
        assertEquals(20, game.getHeight());
        assertTrue(game.isActive());
        assertEquals(1, game.getPlayerDinosaurMap().size());
        Dinosaur d = game.getPlayerDinosaurMap().get("p1");
        assertEquals("d1", d.getId());
        assertEquals("Rex", d.getName());
        assertEquals(5, d.getDamage());
        assertEquals(15, game.getDurationMinutes());
        assertTrue(game.getPowers().contains(Power.HEALING));
    }

    @Test
    void toDTO_null_returnsNull() {
        assertNull(mapper.toDTO((Game) null));
    }

    @Test
    void toDTO_buildsResponseWithDerivedFields() {
        Dinosaur d = Dinosaur.builder().id("d1").name("Rex").damage(5).build();
        Game game = Game.builder()
                .nombre("Partida")
                .boardId("b1")
                .isActive(false)
                .playerDinosaurMap(Map.of("p1", d))
                .powers(Set.of(Power.HEALING))
                .durationMinutes(15)
                .build();
        GameResponseDTO dto = mapper.toDTO(game);
        assertEquals("Partida", dto.getNombre());
        assertEquals("b1", dto.getBoardId());
        assertFalse(dto.isActive());
        assertEquals(1, dto.getPlayerDinosaurMap().size());
        assertTrue(dto.getPowers().contains(Power.HEALING));
        assertEquals(15, dto.getDurationMinutes());
    }

    @Test
    void dinosaurRoundTrip_entityToDtoAndBack() {
        DinosaurRequestDTO dto = mapper.toDTO(Dinosaur.builder().id("d1").name("Rex").damage(5).build());
        assertEquals("d1", dto.getId());
        assertEquals("Rex", dto.getName());
        assertEquals(5, dto.getDamage());

        Dinosaur entity = mapper.toEntity(dto);
        assertEquals("d1", entity.getId());
        assertEquals("Rex", entity.getName());
        assertEquals(5, entity.getDamage());
    }
}
