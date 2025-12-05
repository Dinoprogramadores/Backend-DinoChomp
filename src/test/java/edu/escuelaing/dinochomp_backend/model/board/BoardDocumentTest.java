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
        assertTrue(doc.toString().contains("BoardDocument"));
        assertDoesNotThrow(doc::hashCode);
    }

    @Test
    @DisplayName("equals/hashCode/toString/canEqual generados por Lombok - iguales")
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

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertTrue(a.toString().contains("BoardDocument"));
        assertTrue(a.canEqual(b));
        assertFalse(a.canEqual("otro tipo"));
    }

    @Test
    @DisplayName("equals/hashCode cubren ramas cambiando cada campo")
    void equalsAndHashCodeFieldDifferences() {
        BoardDocument base = new BoardDocument();
        base.setId("id1");
        base.setWidth(2);
        base.setHeight(3);
        base.setCells(new HashMap<>());

        // id distinto
        BoardDocument diffId = new BoardDocument();
        diffId.setId("id2");
        diffId.setWidth(2);
        diffId.setHeight(3);
        diffId.setCells(new HashMap<>());
        assertNotEquals(base, diffId);
        diffId.hashCode();

        // width distinto
        BoardDocument diffWidth = new BoardDocument();
        diffWidth.setId("id1");
        diffWidth.setWidth(99);
        diffWidth.setHeight(3);
        diffWidth.setCells(new HashMap<>());
        assertNotEquals(base, diffWidth);
        diffWidth.hashCode();

        // height distinto
        BoardDocument diffHeight = new BoardDocument();
        diffHeight.setId("id1");
        diffHeight.setWidth(2);
        diffHeight.setHeight(99);
        diffHeight.setCells(new HashMap<>());
        assertNotEquals(base, diffHeight);
        diffHeight.hashCode();

        // cells distinto (contenido)
        Map<String, Object> populated = new HashMap<>();
        populated.put("0,0", "FOOD");
        BoardDocument diffCells = new BoardDocument();
        diffCells.setId("id1");
        diffCells.setWidth(2);
        diffCells.setHeight(3);
        diffCells.setCells(populated);
        assertNotEquals(base, diffCells);
        diffCells.hashCode();

        // id null vs no null
        BoardDocument nullId = new BoardDocument();
        nullId.setId(null);
        nullId.setWidth(2);
        nullId.setHeight(3);
        nullId.setCells(new HashMap<>());
        assertNotEquals(base, nullId);
        nullId.hashCode();

        // ambos id nulos ⇒ iguales
        BoardDocument bothNullId = new BoardDocument();
        bothNullId.setId(null);
        bothNullId.setWidth(2);
        bothNullId.setHeight(3);
        bothNullId.setCells(new HashMap<>());
        assertEquals(nullId, bothNullId);
        assertEquals(nullId.hashCode(), bothNullId.hashCode());

        // cells null vs no null
        BoardDocument nullCells = new BoardDocument();
        nullCells.setId("id1");
        nullCells.setWidth(2);
        nullCells.setHeight(3);
        nullCells.setCells(null);
        assertNotEquals(base, nullCells);
        nullCells.hashCode();

        // ambos cells nulos ⇒ iguales
        BoardDocument bothNullCellsA = new BoardDocument();
        bothNullCellsA.setId("idZ");
        bothNullCellsA.setWidth(2);
        bothNullCellsA.setHeight(3);
        bothNullCellsA.setCells(null);
        BoardDocument bothNullCellsB = new BoardDocument();
        bothNullCellsB.setId("idZ");
        bothNullCellsB.setWidth(2);
        bothNullCellsB.setHeight(3);
        bothNullCellsB.setCells(null);
        assertEquals(bothNullCellsA, bothNullCellsB);
        assertEquals(bothNullCellsA.hashCode(), bothNullCellsB.hashCode());
    }
}