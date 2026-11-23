package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * The pieces in the box for a standard board: 19 terrain hexes and 18 number tokens.
 * Each call returns a new list, so callers can shuffle it.
 */
public final class StandardPieces {
    private static final int[] TOKENS = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12};

    private StandardPieces() {
    }

    /** 4 forest, 3 hills, 4 fields, 4 pasture, 3 mountains, 1 desert. */
    public static List<TerrainType> terrainPool() {
        List<TerrainType> pool = new ArrayList<>();
        add(pool, TerrainType.FOREST, 4);
        add(pool, TerrainType.HILLS, 3);
        add(pool, TerrainType.FIELDS, 4);
        add(pool, TerrainType.PASTURE, 4);
        add(pool, TerrainType.MOUNTAINS, 3);
        add(pool, TerrainType.DESERT, 1);
        return pool;
    }

    /** The 18 resource hexes: the terrain pool without the desert. */
    public static List<TerrainType> resourceTerrainPool() {
        List<TerrainType> pool = terrainPool();
        pool.remove(TerrainType.DESERT);
        return pool;
    }

    /**
     * All 19 terrains in tile-id order: the 18 resource hexes shuffled, and the desert always on
     * the centre tile ({@link HexCoordinate#CENTRE_TILE_ID}).
     */
    public static List<TerrainType> shuffledTerrainsWithDesertInCentre(Random random) {
        List<TerrainType> terrains = resourceTerrainPool();
        Collections.shuffle(terrains, random);
        terrains.add(HexCoordinate.CENTRE_TILE_ID, TerrainType.DESERT);
        return terrains;
    }

    /** 2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12. */
    public static List<Integer> tokenPool() {
        List<Integer> pool = new ArrayList<>();
        for (int t : TOKENS) {
            pool.add(t);
        }
        return pool;
    }

    private static void add(List<TerrainType> pool, TerrainType type, int count) {
        for (int i = 0; i < count; i++) {
            pool.add(type);
        }
    }
}
