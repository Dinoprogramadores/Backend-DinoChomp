package edu.escuelaing.dinochomp_backend.model.board;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(10, 10);
        board.setId("board1");
    }

    @Test
    @DisplayName("Constructor inicializa tamaño y mapa completo")
    void constructorInitializesMap() {
        assertEquals(10, board.getWidth());
        assertEquals(10, board.getHeight());
        assertNotNull(board.getMap());
        assertEquals(10 * 10, board.getMap().size());
        // Todas las celdas deben existir con valor null
        long nulls = board.getMap().values().stream().filter(v -> v == null).count();
        assertEquals(100L, nulls);
    }

    @Test
    @DisplayName("Setters y getters funcionan")
    void settersAndGetters() {
        board.setWidth(25);
        board.setHeight(30);
        board.setComplete(true);

        assertEquals(25, board.getWidth());
        assertEquals(30, board.getHeight());
        assertTrue(board.isComplete());

        Map<Point, BoardItem> newMap = new HashMap<>();
        newMap.put(new Point(0,0), null);
        board.setMap(newMap);
        assertEquals(1, board.getMap().size());

        board.setId("otherId");
        assertEquals("otherId", board.getId());
    }

    @Test
    @DisplayName("Builder de Board crea objeto con valores")
    void builderCreatesBoard() {
        Board built = Board.builder()
                .id("b2")
                .width(3)
                .height(2)
                .map(new HashMap<>())
                .isComplete(false)
                .build();

        assertEquals("b2", built.getId());
        assertEquals(3, built.getWidth());
        assertEquals(2, built.getHeight());
        assertNotNull(built.getMap());
        assertFalse(built.isComplete());
    }

    @Test
    @DisplayName("isNull true dentro del tablero con celda vacía")
    void isNullInsideEmptyCell() {
        Point p = new Point(2, 3);
        assertTrue(board.isNull(p));
    }

    @Test
    @DisplayName("isNull false si el punto está fuera del tablero")
    void isNullOutsideBoard() {
        assertFalse(board.isNull(new Point(10, 10))); // justo fuera
        assertFalse(board.isNull(new Point(-1, 0)));  // negativo X
        assertFalse(board.isNull(new Point(0, -1)));  // negativo Y
        assertFalse(board.isNull(new Point(11, 5)));  // X demasiado grande
        assertFalse(board.isNull(new Point(5, 11)));  // Y demasiado grande
    }

    @Test
    @DisplayName("isNull false cuando hay un BoardItem en la celda")
    void isNullFalseWhenCellHasItem() {
        Point p = new Point(1, 1);
        // Usamos una clase anónima para simular BoardItem
        BoardItem item = new BoardItem() {};
        board.getMap().put(p, item);
        assertFalse(board.isNull(p));
    }

    @Test
    @DisplayName("Cubre equals/hashCode/toString/canEqual generados por Lombok")
    void lombokGeneratedMethods() {
        Board a = Board.builder()
                .id("id1")
                .width(4)
                .height(5)
                .map(new HashMap<>())
                .isComplete(false)
                .build();

        Board b = Board.builder()
                .id("id1")
                .width(4)
                .height(5)
                .map(new HashMap<>())
                .isComplete(false)
                .build();

        Board c = Board.builder()
                .id("id2")
                .width(4)
                .height(5)
                .map(new HashMap<>())
                .isComplete(false)
                .build();

        // equals simétrico y reflexivo
        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertNotEquals(a, c);

        // hashCode consistente con equals
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), c.hashCode());

        // toString no debe ser vacío y debe contener el nombre de la clase
        assertTrue(a.toString().contains("Board"));

        // canEqual: verifica tipo distinto
        Object otherType = new Object();
        assertFalse(a.canEqual(otherType));
        assertTrue(a.canEqual(b));
    }

    @Nested
    class InsideBoardBoundaries {

        static Stream<Point> validPoints() {
            return Stream.of(
                    new Point(0, 0),
                    new Point(9, 0),
                    new Point(0, 9),
                    new Point(9, 9),
                    new Point(5, 5)
            );
        }

        static Stream<Point> invalidPoints() {
            return Stream.of(
                    new Point(-1, 0),
                    new Point(0, -1),
                    new Point(10, 0),
                    new Point(0, 10),
                    new Point(10, 10)
            );
        }

        @ParameterizedTest(name = "isNull true en borde válido {0} cuando la celda es null")
        @MethodSource("validPoints")
        void isNullTrueOnValidBoundaries(Point p) {
            // En el constructor, todas son null
            assertTrue(board.isNull(p));
        }

        @ParameterizedTest(name = "isNull false en punto inválido {0}")
        @MethodSource("invalidPoints")
        void isNullFalseOnInvalidBoundaries(Point p) {
            assertFalse(board.isNull(p));
        }
    }

    @Test
    @DisplayName("Modificar el mapa no rompe las claves Point")
    void putBoardItem() {
        Point p = new Point(1, 1);
        BoardItem item = new BoardItem() {};
        board.getMap().put(p, item);

        assertTrue(board.getMap().containsKey(p));
        assertSame(item, board.getMap().get(p));
    }
}