package edu.escuelaing.dinochomp_backend.model.board;

import org.springframework.data.annotation.Id;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

import java.awt.Point;
import java.io.Serializable;
import java.util.*;

@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "Boards")
@Getter
@Setter
public class Board implements Serializable {

    @Id
    private String id;
    private int width;
    private int height;
    private Map<Point, BoardItem> map;
    private boolean isComplete = false;

    public Board(int width, int height) {
        this.width = width;
        this.height = height;
        this.map = new HashMap<>();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map.put(new Point(x, y), null);
            }
        }
    }

    public boolean isNull(Point p) {
        return insideBoard(p) && map.get(p) == null;
    }

    private boolean insideBoard(Point p) {
        return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
    }
}

