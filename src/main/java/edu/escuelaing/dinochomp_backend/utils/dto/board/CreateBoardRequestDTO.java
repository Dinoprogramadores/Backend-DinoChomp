package edu.escuelaing.dinochomp_backend.utils.dto.board;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateBoardRequestDTO {
    private int width;
    private int height;
}
