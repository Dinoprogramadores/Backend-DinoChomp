package edu.escuelaing.dinochomp_backend.utils.mappers;

import edu.escuelaing.dinochomp_backend.model.board.Board;
import edu.escuelaing.dinochomp_backend.model.board.BoardDocument;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;

public class BoardMapper {

    public static BoardDocument toDocument(Board board) {
        BoardDocument doc = new BoardDocument();
        doc.setId(board.getId());
        doc.setWidth(board.getWidth());
        doc.setHeight(board.getHeight());

        Map<String, Object> serialized = new HashMap<>();
        for (Map.Entry<Point, Object> entry : board.getMap().entrySet()) {
            Point p = entry.getKey();
            serialized.put(p.x + "," + p.y, entry.getValue());
        }
        doc.setCells(serialized);

        return doc;
    }

    public static Board fromDocument(BoardDocument doc) {
        Board board = new Board(doc.getWidth(), doc.getHeight());
        board.setId(doc.getId());

        Map<Point, Object> deserialized = new HashMap<>();
        for (Map.Entry<String, Object> entry : doc.getCells().entrySet()) {
            String[] parts = entry.getKey().split(",");
            Point p = new Point(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            deserialized.put(p, entry.getValue());
        }
        board.setMap(deserialized);

        return board;
    }
}