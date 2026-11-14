package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TileTest {
    @Test
    void resourceTileExposesTerrainResourceAndToken() {
        Tile t = new Tile(4, TerrainType.FOREST, 6);
        assertEquals(4, t.getId());
        assertEquals(TerrainType.FOREST, t.getTerrain());
        assertEquals(ResourceType.LUMBER, t.getResourceType());
        assertEquals(6, t.getNumberToken());
        assertTrue(t.hasNumberToken());
    }

    @Test
    void terrainMapsToResource() {
        assertEquals(ResourceType.LUMBER, Tile.resourceFor(TerrainType.FOREST));
        assertEquals(ResourceType.BRICK, Tile.resourceFor(TerrainType.HILLS));
        assertEquals(ResourceType.GRAIN, Tile.resourceFor(TerrainType.FIELDS));
        assertEquals(ResourceType.WOOL, Tile.resourceFor(TerrainType.PASTURE));
        assertEquals(ResourceType.ORE, Tile.resourceFor(TerrainType.MOUNTAINS));
        assertNull(Tile.resourceFor(TerrainType.DESERT));
    }

    @Test
    void desertHasNoResourceOrToken() {
        Tile t = new Tile(0, TerrainType.DESERT, Tile.NO_TOKEN);
        assertNull(t.getResourceType());
        assertEquals(Tile.NO_TOKEN, t.getNumberToken());
        assertFalse(t.hasNumberToken());
    }

    @Test
    void invalidTokensRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, TerrainType.HILLS, 7));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, TerrainType.HILLS, 1));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, TerrainType.HILLS, 13));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, TerrainType.HILLS, Tile.NO_TOKEN));
        assertThrows(IllegalArgumentException.class, () -> new Tile(0, TerrainType.DESERT, 6));
    }

    @Test
    void nullTerrainRejected() {
        assertThrows(NullPointerException.class, () -> new Tile(0, null, 6));
    }

    @Test
    void vertexAndEdgeListsAreUnmodifiable() {
        Tile t = new Tile(0, TerrainType.FIELDS, 5);
        assertTrue(t.getVertices().isEmpty());
        assertTrue(t.getEdges().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> t.getVertices().add(new Vertex(0)));
        assertThrows(UnsupportedOperationException.class, () -> t.getEdges().clear());
    }
}
