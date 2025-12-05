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
        long nulls = board.getMap().values().stream().filter(v -> v == null).count();
        assertEquals(100L, nulls);
    }

    @Test
    @DisplayName("Constructor sin argumentos deja valores por defecto")
    void noArgsConstructorDefaults() {
        Board empty = new Board(); // cubre Board()
        assertNull(empty.getId());
        assertEquals(0, empty.getWidth());
        assertEquals(0, empty.getHeight());
        assertNull(empty.getMap()); // Lombok no inicializa aquí
        assertFalse(empty.isComplete());
        // Invocar hashCode/toString aun con nulos para cubrir ramas
        assertNotNull(empty.toString());
        assertDoesNotThrow(empty::hashCode);
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
        BoardItem item = new BoardItem() {};
        board.getMap().put(p, item);
        assertFalse(board.isNull(p));
    }

    @Test
    @DisplayName("equals/hashCode/toString/canEqual generados por Lombok - iguales")
    void lombokGeneratedMethodsEquals() {
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

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode()); // hashCode consistente
        assertTrue(a.toString().contains("Board"));
        assertTrue(a.canEqual(b));
    }

    @Test
    @DisplayName("equals/hashCode cubren ramas cambiando cada campo")
    void equalsAndHashCodeFieldDifferences() {
        Board base = Board.builder()
                .id("id1")
                .width(2)
                .height(3)
                .map(new HashMap<>())
                .isComplete(false)
                .build();

        // id distinto
        Board diffId = Board.builder()
                .id("id2")
                .width(2).height(3)
                .map(new HashMap<>())
                .isComplete(false)
                .build();
        assertNotEquals(base, diffId);
        diffId.hashCode(); // ejecuta rama de id !=

        // width distinto
        Board diffWidth = Board.builder()
                .id("id1")
                .width(99).height(3)
                .map(new HashMap<>())
                .isComplete(false)
                .build();
        assertNotEquals(base, diffWidth);
        diffWidth.hashCode();

        // height distinto
        Board diffHeight = Board.builder()
                .id("id1")
                .width(2).height(99)
                .map(new HashMap<>())
                .isComplete(false)
                .build();
        assertNotEquals(base, diffHeight);
        diffHeight.hashCode();

        // isComplete distinto
        Board diffComplete = Board.builder()
                .id("id1")
                .width(2).height(3)
                .map(new HashMap<>())
                .isComplete(true)
                .build();
        assertNotEquals(base, diffComplete);
        diffComplete.hashCode();

        // map distinto (contenido)
        Map<Point, BoardItem> populated = new HashMap<>();
        populated.put(new Point(0,0), new BoardItem() {});
        Board diffMapContent = Board.builder()
                .id("id1")
                .width(2).height(3)
                .map(populated)
                .isComplete(false)
                .build();
        assertNotEquals(base, diffMapContent);
        diffMapContent.hashCode();

        // map null vs no null (rama map == null)
        Board nullMap = Board.builder()
                .id("id1")
                .width(2).height(3)
                .map(null)
                .isComplete(false)
                .build();
        assertNotEquals(base, nullMap);
        nullMap.hashCode();

        // id null vs no null (rama id == null)
        Board nullId = Board.builder()
                .id(null)
                .width(2).height(3)
                .map(new HashMap<>())
                .isComplete(false)
                .build();
        assertNotEquals(base, nullId);
        nullId.hashCode();

        // ambos nulos en el mismo campo ⇒ iguales
        Board bothNullId = Board.builder()
                .id(null)
                .width(2).height(3)
                .map(new HashMap<>())
                .isComplete(false)
                .build();
        assertEquals(nullId, bothNullId);
        assertEquals(nullId.hashCode(), bothNullId.hashCode());

        Board bothNullMapA = Board.builder()
                .id("idX")
                .width(2).height(3)
                .map(null)
                .isComplete(false)
                .build();
        Board bothNullMapB = Board.builder()
                .id("idX")
                .width(2).height(3)
                .map(null)
                .isComplete(false)
                .build();
        assertEquals(bothNullMapA, bothNullMapB);
        assertEquals(bothNullMapA.hashCode(), bothNullMapB.hashCode());
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