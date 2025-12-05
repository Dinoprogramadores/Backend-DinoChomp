package edu.escuelaing.dinochomp_backend.utils.dto.board;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CreateBoardRequestDTOTest {

    @Test
    void builder_setsAllFields() {
        CreateBoardRequestDTO dto = CreateBoardRequestDTO.builder()
                .width(10)
                .height(20)
                .build();
        assertEquals(10, dto.getWidth());
        assertEquals(20, dto.getHeight());
    }

    @Test
    void noArgsConstructor_andSetters_work() {
        CreateBoardRequestDTO dto = new CreateBoardRequestDTO();
        dto.setWidth(3);
        dto.setHeight(4);
        assertEquals(3, dto.getWidth());
        assertEquals(4, dto.getHeight());
    }

    @Test
    void allArgsConstructor_buildsProperly() {
        CreateBoardRequestDTO dto = new CreateBoardRequestDTO(7,8);
        assertEquals(7, dto.getWidth());
        assertEquals(8, dto.getHeight());
    }
}

