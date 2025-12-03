package edu.escuelaing.dinochomp_backend.model.board;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BoardDocumentTest {

    @Test
    @DisplayName("Getters y setters de BoardDocument")
    void gettersSetters() {
        BoardDocument doc = new BoardDocument();
        doc.setId("doc1");
        doc.setWidth(7);
        doc.setHeight(3);

        Map<String, Object> cells = new HashMap<>();
        cells.put("0,0", null);
        cells.put("1,2", "FOOD");
        doc.setCells(cells);

        assertEquals("doc1", doc.getId());
        assertEquals(7, doc.getWidth());
        assertEquals(3, doc.getHeight());
        assertEquals(2, doc.getCells().size());
        assertTrue(doc.getCells().containsKey("1,2"));
    }

    @Test
    @DisplayName("equals/hashCode/toString/canEqual generados por Lombok")
    void lombokGeneratedMethods() {
        BoardDocument a = new BoardDocument();
        a.setId("docX");
        a.setWidth(5);
        a.setHeight(5);
        a.setCells(new HashMap<>());

        BoardDocument b = new BoardDocument();
        b.setId("docX");
        b.setWidth(5);
        b.setHeight(5);
        b.setCells(new HashMap<>());

        BoardDocument c = new BoardDocument();
        c.setId("docY");
        c.setWidth(5);
        c.setHeight(5);
        c.setCells(new HashMap<>());

        assertEquals(a, b);
        assertNotEquals(a, c);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());
        assertTrue(a.toString().contains("BoardDocument"));
        assertTrue(a.canEqual(b));
        assertFalse(a.canEqual("otro tipo"));
    }
}