package entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import org.junit.jupiter.api.Test;

class HexCoordinateTest {
    @Test
    void standardBoardHasNineteenDistinctTilesWithinRadiusTwo() {
        List<HexCoordinate> coords = HexCoordinate.standardBoard();
        assertEquals(19, coords.size());
        assertEquals(19, new HashSet<>(coords).size());
        for (HexCoordinate c : coords) {
            assertTrue(c.distanceTo(new HexCoordinate(0, 0)) <= 2);
        }
    }

    @Test
    void tileNineIsTheCentreAndOrderIsRowByRow() {
        assertEquals(new HexCoordinate(0, 0), HexCoordinate.ofStandardTile(9));
        assertEquals(new HexCoordinate(0, -2), HexCoordinate.ofStandardTile(0));
        assertEquals(new HexCoordinate(0, 2), HexCoordinate.ofStandardTile(18));
    }

    @Test
    void unknownTileIdThrows() {
        assertThrows(IllegalArgumentException.class, () -> HexCoordinate.ofStandardTile(19));
        assertThrows(IllegalArgumentException.class, () -> HexCoordinate.ofStandardTile(-1));
    }

    @Test
    void neighboursAreSixAdjacentHexes() {
        HexCoordinate centre = new HexCoordinate(0, 0);
        assertEquals(6, centre.neighbours().size());
        for (HexCoordinate n : centre.neighbours()) {
            assertTrue(centre.isAdjacentTo(n));
            assertEquals(1, centre.distanceTo(n));
        }
        assertFalse(centre.isAdjacentTo(centre));
        assertFalse(centre.isAdjacentTo(new HexCoordinate(2, -1)));
    }

    @Test
    void adjacentTilesOnTheBoardShareAnEdge() {
        Board board = BoardFactory.createStandardBoard(new java.util.Random(3));
        for (Tile a : board.getTiles()) {
            for (Tile b : board.getTiles()) {
                if (a == b) {
                    continue;
                }
                boolean shareEdge = a.getEdges().stream().anyMatch(b.getEdges()::contains);
                boolean adjacent = HexCoordinate.ofStandardTile(a.getId())
                        .isAdjacentTo(HexCoordinate.ofStandardTile(b.getId()));
                assertEquals(adjacent, shareEdge, "tiles " + a.getId() + " and " + b.getId());
            }
        }
    }
}
