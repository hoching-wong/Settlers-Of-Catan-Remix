package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class BoardLayoutTest {
    @Test
    void assignTokensSkipsTheDesert() {
        BoardLayout layout = BoardLayout.assignTokens(
                List.of(TerrainType.FOREST, TerrainType.DESERT, TerrainType.HILLS), List.of(5, 9));
        assertEquals(List.of(5, Tile.NO_TOKEN, 9), layout.getNumberTokens());
        assertEquals(TerrainType.DESERT, layout.terrainAt(1));
        assertEquals(9, layout.numberTokenAt(2));
        assertEquals(3, layout.size());
    }

    @Test
    void wrongNumberOfTokensThrows() {
        assertThrows(IllegalArgumentException.class, () -> BoardLayout.assignTokens(
                List.of(TerrainType.FOREST, TerrainType.HILLS), List.of(5)));
        assertThrows(IllegalArgumentException.class, () -> BoardLayout.assignTokens(
                List.of(TerrainType.FOREST), List.of(5, 6)));
        assertThrows(IllegalArgumentException.class, () -> new BoardLayout(
                List.of(TerrainType.FOREST), List.of(5, 6)));
    }

    @Test
    void onlyTheDesertMayHaveNoToken() {
        assertThrows(IllegalArgumentException.class,
                () -> new BoardLayout(List.of(TerrainType.FOREST), List.of(Tile.NO_TOKEN)));
        assertThrows(IllegalArgumentException.class,
                () -> new BoardLayout(List.of(TerrainType.DESERT), List.of(6)));
    }

    @Test
    void factoryBuildsExactlyTheGivenLayout() {
        BoardLayout layout = new RandomLayoutStrategy().createLayout(new java.util.Random(11));
        Board board = BoardFactory.createBoard(layout);
        for (Tile t : board.getTiles()) {
            assertEquals(layout.terrainAt(t.getId()), t.getTerrain());
            assertEquals(layout.numberTokenAt(t.getId()), t.getNumberToken());
        }
    }

    @Test
    void factoryRejectsALayoutOfTheWrongSize() {
        BoardLayout tiny = BoardLayout.assignTokens(List.of(TerrainType.FOREST), List.of(5));
        assertThrows(IllegalArgumentException.class, () -> BoardFactory.createBoard(tiny));
    }
}
