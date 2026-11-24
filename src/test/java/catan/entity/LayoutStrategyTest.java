package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class LayoutStrategyTest {
    private static void assertStandardPieces(BoardLayout layout) {
        assertEquals(19, layout.size());
        Map<TerrainType, Integer> counts = new EnumMap<>(TerrainType.class);
        List<Integer> tokens = new ArrayList<>();
        for (int i = 0; i < layout.size(); i++) {
            counts.merge(layout.terrainAt(i), 1, Integer::sum);
            if (layout.numberTokenAt(i) != Tile.NO_TOKEN) {
                tokens.add(layout.numberTokenAt(i));
            }
        }
        assertEquals(4, counts.get(TerrainType.FOREST));
        assertEquals(3, counts.get(TerrainType.HILLS));
        assertEquals(4, counts.get(TerrainType.FIELDS));
        assertEquals(4, counts.get(TerrainType.PASTURE));
        assertEquals(3, counts.get(TerrainType.MOUNTAINS));
        assertEquals(1, counts.get(TerrainType.DESERT));
        tokens.sort(null);
        assertEquals(StandardPieces.tokenPool(), tokens);
    }

    @Test
    void bothStrategiesUseTheStandardPieces() {
        for (long seed = 0; seed < 20; seed++) {
            assertStandardPieces(new RandomLayoutStrategy().createLayout(new Random(seed)));
            assertStandardPieces(new BalancedLayoutStrategy().createLayout(new Random(seed)));
        }
    }

    @Test
    void balancedNeverPutsSixAndEightNextToEachOther() {
        for (long seed = 0; seed < 300; seed++) {
            BoardLayout layout = new BalancedLayoutStrategy().createLayout(new Random(seed));
            assertFalse(BalancedLayoutStrategy.hasAdjacentRedNumbers(layout), "seed " + seed);
        }
    }

    @Test
    void plainRandomSometimesDoesPutThemTogether() {
        boolean found = false;
        for (long seed = 0; seed < 300 && !found; seed++) {
            found = BalancedLayoutStrategy.hasAdjacentRedNumbers(
                    new RandomLayoutStrategy().createLayout(new Random(seed)));
        }
        assertTrue(found, "the balanced rule should be doing something");
    }

    @Test
    void sameSeedSameLayoutDifferentSeedsDiffer() {
        BoardLayoutStrategy s = new BalancedLayoutStrategy();
        assertEquals(s.createLayout(new Random(5)).getNumberTokens(), s.createLayout(new Random(5)).getNumberTokens());
        assertEquals(s.createLayout(new Random(5)).getTerrains(), s.createLayout(new Random(5)).getTerrains());
        Set<List<TerrainType>> seen = new HashSet<>();
        for (long seed = 0; seed < 10; seed++) {
            seen.add(s.createLayout(new Random(seed)).getTerrains());
        }
        assertTrue(seen.size() > 1);
    }

    @Test
    void redNumberCheckSeesAdjacency() {
        List<TerrainType> terrains = StandardPieces.terrainPool(); // desert is last (tile 18)
        List<Integer> tokens = new ArrayList<>(List.of(6, 8, 2, 3, 3, 4, 4, 5, 5, 6, 8, 9, 9, 10, 10, 11, 11, 12));
        // tiles 0 and 1 are neighbours on the top row
        assertTrue(BalancedLayoutStrategy.hasAdjacentRedNumbers(BoardLayout.assignTokens(terrains, tokens)));
        assertTrue(BalancedLayoutStrategy.isRed(6));
        assertTrue(BalancedLayoutStrategy.isRed(8));
        assertFalse(BalancedLayoutStrategy.isRed(5));
    }

    @Test
    void createBoardWithStrategyUsesIt() {
        Board board = BoardFactory.createBoard(new BalancedLayoutStrategy(), new Random(9));
        BoardLayout expected = new BalancedLayoutStrategy().createLayout(new Random(9));
        for (Tile t : board.getTiles()) {
            assertEquals(expected.terrainAt(t.getId()), t.getTerrain());
            assertEquals(expected.numberTokenAt(t.getId()), t.getNumberToken());
        }
    }

    @Test
    void desertIsAlwaysTheCentreTile() {
        assertEquals(9, HexCoordinate.CENTRE_TILE_ID);
        for (long seed = 0; seed < 100; seed++) {
            for (BoardLayoutStrategy s : List.of(new RandomLayoutStrategy(), new BalancedLayoutStrategy())) {
                BoardLayout layout = s.createLayout(new Random(seed));
                assertEquals(TerrainType.DESERT, layout.terrainAt(HexCoordinate.CENTRE_TILE_ID));
                assertEquals(Tile.NO_TOKEN, layout.numberTokenAt(HexCoordinate.CENTRE_TILE_ID));
            }
            Board board = BoardFactory.createStandardBoard(new Random(seed));
            Tile centre = board.getTile(HexCoordinate.CENTRE_TILE_ID);
            assertEquals(TerrainType.DESERT, centre.getTerrain());
            assertEquals(new HexCoordinate(0, 0), HexCoordinate.ofStandardTile(centre.getId()));
        }
    }

    @Test
    void outerTilesStillChangeBetweenGames() {
        Set<List<TerrainType>> seen = new HashSet<>();
        for (long seed = 0; seed < 10; seed++) {
            seen.add(StandardPieces.shuffledTerrainsWithDesertInCentre(new Random(seed)));
        }
        assertTrue(seen.size() > 1);
        assertEquals(18, StandardPieces.resourceTerrainPool().size());
        assertFalse(StandardPieces.resourceTerrainPool().contains(TerrainType.DESERT));
    }
}
