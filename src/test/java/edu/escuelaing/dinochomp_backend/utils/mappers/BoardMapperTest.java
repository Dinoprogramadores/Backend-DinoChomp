package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;
import edu.escuelaing.dinochomp_backend.model.board.BoardItem;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardMapperTest {

    @Test
    void toDocument_serializesBoard() {
        Board board = new Board(5, 6);
        board.setId("b1");
        Map<Point, BoardItem> map = new HashMap<>();
        // Para la prueba, no es necesario un BoardItem concreto; usamos null y verificamos claves
        map.put(new Point(1, 2), null);
        board.setMap(map);

        BoardDocument doc = BoardMapper.toDocument(board);
        assertEquals("b1", doc.getId());
        assertEquals(5, doc.getWidth());
        assertEquals(6, doc.getHeight());
        assertTrue(doc.getCells().containsKey("1,2"));
    }

    @Test
    void fromDocument_deserializesBoardItems_andHandlesNonBoardItemAsNull() {
        BoardDocument doc = new BoardDocument();
        doc.setId("b1");
        doc.setWidth(5);
        doc.setHeight(6);
        Map<String, Object> cells = new HashMap<>();
        // Valor que es BoardItem y otro que no lo es
        BoardItem item = new BoardItem() { };
        cells.put("2,3", item);
        cells.put("4,5", "notBoardItem");
        doc.setCells(cells);

        Board board = BoardMapper.fromDocument(doc);
        assertEquals("b1", board.getId());
        assertEquals(5, board.getWidth());
        assertEquals(6, board.getHeight());
        assertTrue(board.getMap().containsKey(new Point(2, 3)));
        assertTrue(board.getMap().containsKey(new Point(4, 5)));
        assertEquals(item, board.getMap().get(new Point(2, 3)));
        assertNull(board.getMap().get(new Point(4, 5)));
    }
}

