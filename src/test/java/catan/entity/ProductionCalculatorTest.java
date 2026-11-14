package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class ProductionCalculatorTest {
    private final Player ann = new Player("Ann");
    private final Player bob = new Player("Bob");
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();

    private Vertex newVertex() {
        Vertex v = new Vertex(vertices.size());
        vertices.add(v);
        return v;
    }

    /** A tile with its own 6 vertices, plus any shared ones swapped in at the start. */
    private Tile tile(TerrainType terrain, int token, Vertex... shared) {
        Tile t = new Tile(tiles.size(), terrain, token);
        for (Vertex v : shared) {
            t.addVertex(v);
        }
        while (t.getVertices().size() < 6) {
            t.addVertex(newVertex());
        }
        tiles.add(t);
        return t;
    }

    private Board board() {
        return new Board(tiles, vertices, List.of());
    }

    private void settle(Player p, Vertex v) {
        v.setBuilding(new Settlement(p, v));
        p.addBuilding(v.getBuilding());
    }

    private void upgrade(Player p, Vertex v) {
        v.setBuilding(new City(p, v));
    }

    private Map<Player, Map<ResourceType, Integer>> roll(int total, WeatherData weather) {
        return ProductionCalculator.distribute(board(), total, weather);
    }

    @Test
    void settlementProducesOne() {
        Tile t = tile(TerrainType.FOREST, 6);
        settle(ann, t.getVertices().get(0));
        assertEquals(Map.of(ann, Map.of(ResourceType.LUMBER, 1)), roll(6, WeatherData.none()));
    }

    @Test
    void cityProducesTwo() {
        Tile t = tile(TerrainType.MOUNTAINS, 8);
        upgrade(ann, t.getVertices().get(2));
        assertEquals(Map.of(ann, Map.of(ResourceType.ORE, 2)), roll(8, WeatherData.none()));
    }

    @Test
    void everyPlayerOnTheTileIsPaid() {
        Tile t = tile(TerrainType.FIELDS, 5);
        settle(ann, t.getVertices().get(0));
        upgrade(bob, t.getVertices().get(3));
        assertEquals(Map.of(ann, Map.of(ResourceType.GRAIN, 1), bob, Map.of(ResourceType.GRAIN, 2)),
                roll(5, WeatherData.none()));
    }

    @Test
    void sameTileCornersAddUp() {
        Tile t = tile(TerrainType.PASTURE, 9);
        settle(ann, t.getVertices().get(0));
        settle(ann, t.getVertices().get(3));
        upgrade(ann, t.getVertices().get(4));
        assertEquals(Map.of(ann, Map.of(ResourceType.WOOL, 4)), roll(9, WeatherData.none()));
    }

    @Test
    void vertexSharedByTwoRolledTilesPaysBoth() {
        Vertex shared = newVertex();
        tile(TerrainType.FOREST, 4, shared);
        tile(TerrainType.HILLS, 4, shared);
        settle(ann, shared);
        assertEquals(Map.of(ann, Map.of(ResourceType.LUMBER, 1, ResourceType.BRICK, 1)),
                roll(4, WeatherData.none()));
    }

    @Test
    void onlyTheRolledTileProduces() {
        Vertex shared = newVertex();
        tile(TerrainType.FOREST, 4, shared);
        tile(TerrainType.HILLS, 10, shared);
        settle(ann, shared);
        assertEquals(Map.of(ann, Map.of(ResourceType.LUMBER, 1)), roll(4, WeatherData.none()));
        assertEquals(Map.of(ann, Map.of(ResourceType.BRICK, 1)), roll(10, WeatherData.none()));
    }

    @Test
    void sevenProducesNothing() {
        for (TerrainType terrain : new TerrainType[] {TerrainType.FOREST, TerrainType.HILLS}) {
            Tile t = tile(terrain, 6);
            settle(ann, t.getVertices().get(0));
        }
        assertTrue(roll(7, WeatherData.none()).isEmpty());
        assertTrue(roll(7, WeatherData.fromWmoCode(0)).isEmpty());
    }

    @Test
    void nothingProducedWhenNobodyIsAdjacent() {
        tile(TerrainType.FOREST, 6);
        assertTrue(roll(6, WeatherData.none()).isEmpty());
    }

    @Test
    void nothingProducedWhenNoTileMatches() {
        Tile t = tile(TerrainType.FOREST, 6);
        settle(ann, t.getVertices().get(0));
        assertTrue(roll(5, WeatherData.none()).isEmpty());
    }

    @Test
    void desertNeverProduces() {
        Tile t = tile(TerrainType.DESERT, Tile.NO_TOKEN);
        settle(ann, t.getVertices().get(0));
        assertTrue(roll(0, WeatherData.none()).isEmpty());
        for (int total = 2; total <= 12; total++) {
            assertTrue(roll(total, WeatherData.none()).isEmpty());
        }
    }

    @Test
    void weatherDoublesOnlyTheBonusResource() {
        Vertex shared = newVertex();
        tile(TerrainType.FOREST, 6, shared);
        tile(TerrainType.HILLS, 6, shared);
        settle(ann, shared);
        WeatherData sunny = WeatherData.fromWmoCode(0); // lumber x2
        assertEquals(Map.of(ann, Map.of(ResourceType.LUMBER, 2, ResourceType.BRICK, 1)), roll(6, sunny));
    }

    @Test
    void weatherMultipliesCityProductionToo() {
        Tile t = tile(TerrainType.MOUNTAINS, 3);
        upgrade(ann, t.getVertices().get(0));
        WeatherData stormy = WeatherData.fromWmoCode(95); // ore x2
        assertEquals(Map.of(ann, Map.of(ResourceType.ORE, 4)), roll(3, stormy));
    }

    @Test
    void weatherBonusAppliesToEveryPlayer() {
        Tile t = tile(TerrainType.FIELDS, 11);
        settle(ann, t.getVertices().get(0));
        settle(bob, t.getVertices().get(3));
        WeatherData rainy = WeatherData.fromWmoCode(61); // grain x2
        assertEquals(Map.of(ann, Map.of(ResourceType.GRAIN, 2), bob, Map.of(ResourceType.GRAIN, 2)),
                roll(11, rainy));
    }

    @Test
    void weatherBonusNeedsTheMatchingTileToBeRolled() {
        Tile forest = tile(TerrainType.FOREST, 6);
        Tile hills = tile(TerrainType.HILLS, 8);
        settle(ann, forest.getVertices().get(0));
        settle(ann, hills.getVertices().get(0));
        WeatherData sunny = WeatherData.fromWmoCode(1); // lumber x2
        assertEquals(Map.of(ann, Map.of(ResourceType.BRICK, 1)), roll(8, sunny));
        assertEquals(Map.of(ann, Map.of(ResourceType.LUMBER, 2)), roll(6, sunny));
    }

    @Test
    void weatherWithoutBonusChangesNothing() {
        Tile t = tile(TerrainType.FOREST, 6);
        settle(ann, t.getVertices().get(0));
        Map<Player, Map<ResourceType, Integer>> normal = Map.of(ann, Map.of(ResourceType.LUMBER, 1));
        assertEquals(normal, roll(6, WeatherData.none()));
        assertEquals(normal, roll(6, WeatherData.fromWmoCode(45))); // fog
        assertEquals(normal, roll(6, WeatherData.fromWmoCode(1000))); // no category
    }

    @Test
    void distributeDoesNotChangeAnyHand() {
        Tile t = tile(TerrainType.FOREST, 6);
        settle(ann, t.getVertices().get(0));
        roll(6, WeatherData.none());
        assertEquals(0, ann.getResources().getTotalResources());
    }

    @Test
    void resultCannotBeModified() {
        Tile t = tile(TerrainType.FOREST, 6);
        settle(ann, t.getVertices().get(0));
        Map<Player, Map<ResourceType, Integer>> result = roll(6, WeatherData.none());
        assertThrows(UnsupportedOperationException.class, () -> result.put(bob, Map.of()));
        assertThrows(UnsupportedOperationException.class,
                () -> result.get(ann).put(ResourceType.ORE, 1));
    }

    @Test
    void nullArgumentsRejected() {
        assertThrows(NullPointerException.class,
                () -> ProductionCalculator.distribute(null, 6, WeatherData.none()));
        assertThrows(NullPointerException.class, () -> ProductionCalculator.distribute(board(), 6, null));
    }

    @Test
    void worksOnAStandardBoard() {
        Board standard = BoardFactory.createStandardBoard(new Random(3));
        Tile target = null;
        for (Tile t : standard.getTiles()) {
            if (t.hasNumberToken()) {
                target = t;
                break;
            }
        }
        Vertex v = target.getVertices().get(0);
        standard.placeBuilding(v, new Settlement(ann, v));

        Map<Player, Map<ResourceType, Integer>> result =
                ProductionCalculator.distribute(standard, target.getNumberToken(), WeatherData.none());
        assertTrue(result.get(ann).get(target.getResourceType()) >= 1);

        int total = 0;
        for (int dice = 2; dice <= 12; dice++) {
            Map<ResourceType, Integer> gains =
                    ProductionCalculator.distribute(standard, dice, WeatherData.none()).get(ann);
            if (gains != null) {
                total += gains.values().stream().mapToInt(Integer::intValue).sum();
            }
        }
        // one settlement touches 1 to 3 tiles, each with one token, desert excluded
        assertTrue(total >= 1 && total <= 3);
    }
}
