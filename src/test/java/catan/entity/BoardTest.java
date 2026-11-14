package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BoardTest {
    private final Board board = BoardFactory.createStandardBoard(new Random(1));

    @Test
    void lookupsReturnItemsById() {
        for (Tile t : board.getTiles()) {
            assertSame(t, board.getTile(t.getId()));
        }
        for (Vertex v : board.getVertices()) {
            assertSame(v, board.getVertex(v.getId()));
        }
        for (Edge e : board.getEdges()) {
            assertSame(e, board.getEdge(e.getId()));
        }
    }

    @Test
    void unknownIdsRejected() {
        assertThrows(IllegalArgumentException.class, () -> board.getTile(19));
        assertThrows(IllegalArgumentException.class, () -> board.getVertex(54));
        assertThrows(IllegalArgumentException.class, () -> board.getEdge(72));
        assertThrows(IllegalArgumentException.class, () -> board.getTile(-1));
    }

    @Test
    void listsAreUnmodifiable() {
        assertThrows(UnsupportedOperationException.class, () -> board.getTiles().clear());
        assertThrows(UnsupportedOperationException.class, () -> board.getVertices().clear());
        assertThrows(UnsupportedOperationException.class, () -> board.getEdges().clear());
    }

    @Test
    void matchingTilesHaveTheRolledToken() {
        for (int total = 2; total <= 12; total++) {
            for (Tile t : board.getMatchingTiles(total)) {
                assertEquals(total, t.getNumberToken());
            }
        }
        int matched = 0;
        for (int total = 2; total <= 12; total++) {
            matched += board.getMatchingTiles(total).size();
        }
        assertEquals(18, matched, "every resource tile matches exactly one total");
    }

    @Test
    void sevenAndOutOfRangeTotalsMatchNothing() {
        assertTrue(board.getMatchingTiles(7).isEmpty());
        assertTrue(board.getMatchingTiles(0).isEmpty(), "the desert must not match 0");
        assertTrue(board.getMatchingTiles(13).isEmpty());
    }

    @Test
    void twoAndTwelveHaveOneTileAndOthersHaveTwo() {
        assertEquals(1, board.getMatchingTiles(2).size());
        assertEquals(1, board.getMatchingTiles(12).size());
        for (int total : new int[] {3, 4, 5, 6, 8, 9, 10, 11}) {
            assertEquals(2, board.getMatchingTiles(total).size(), "total " + total);
        }
    }

    @Test
    void tilesAdjacentToVertexAreExactlyTheTilesContainingIt() {
        for (Vertex v : board.getVertices()) {
            List<Tile> adjacent = board.getTilesAdjacentTo(v);
            assertFalse(adjacent.isEmpty());
            assertTrue(adjacent.size() <= 3);
            for (Tile t : board.getTiles()) {
                assertEquals(t.getVertices().contains(v), adjacent.contains(t));
            }
        }
    }

    @Test
    void handBuiltBoardWorksToo() {
        Tile a = new Tile(10, TerrainType.HILLS, 6);
        Tile b = new Tile(11, TerrainType.DESERT, Tile.NO_TOKEN);
        Vertex v = new Vertex(20);
        a.addVertex(v);
        Board small = new Board(List.of(a, b), List.of(v), List.of());

        assertSame(a, small.getTile(10));
        assertEquals(List.of(a), small.getMatchingTiles(6));
        assertEquals(List.of(a), small.getTilesAdjacentTo(v));
        assertTrue(small.getTilesAdjacentTo(new Vertex(99)).isEmpty());
    }
}
