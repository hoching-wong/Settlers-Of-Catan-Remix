package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BoardFactory {
    private static final int RADIUS = 2;

    private static final TerrainType[] TERRAIN_POOL = buildTerrainPool();
    private static final int[] TOKEN_POOL = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12};

    private record Point(long x, long y) {}

    /**
     * Creates the 19 tiles, 54 vertices and 72 edges, wires up adjacency, and shuffles the
     * terrain and number tokens with the given Random.
     */
    public static Board createStandardBoard(Random random) {
        List<TerrainType> terrains = new ArrayList<>(List.of(TERRAIN_POOL));
        Collections.shuffle(terrains, random);
        List<Integer> tokens = new ArrayList<>();
        for (int t : TOKEN_POOL) {
            tokens.add(t);
        }
        Collections.shuffle(tokens, random);

        List<Tile> tiles = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<Point, Vertex> vertexByPoint = new HashMap<>();
        Map<Long, Edge> edgeByVertexPair = new HashMap<>();

        int tokenIndex = 0;
        int tileId = 0;
        for (int r = -RADIUS; r <= RADIUS; r++) {
            for (int q = -RADIUS; q <= RADIUS; q++) {
                if (Math.abs(q + r) > RADIUS) {
                    continue;
                }
                TerrainType terrain = terrains.get(tileId);
                int token = terrain == TerrainType.DESERT ? Tile.NO_TOKEN : tokens.get(tokenIndex++);
                Tile tile = new Tile(tileId++, terrain, token);

                Vertex[] corners = new Vertex[6];
                for (int i = 0; i < 6; i++) {
                    corners[i] = vertexAt(cornerPoint(q, r, i), vertexByPoint, vertices);
                    tile.addVertex(corners[i]);
                }
                for (int i = 0; i < 6; i++) {
                    tile.addEdge(edgeBetween(corners[i], corners[(i + 1) % 6], edgeByVertexPair, edges));
                }
                tiles.add(tile);
            }
        }
        return new Board(tiles, vertices, edges);
    }

    private static TerrainType[] buildTerrainPool() {
        List<TerrainType> pool = new ArrayList<>();
        add(pool, TerrainType.FOREST, 4);
        add(pool, TerrainType.HILLS, 3);
        add(pool, TerrainType.FIELDS, 4);
        add(pool, TerrainType.PASTURE, 4);
        add(pool, TerrainType.MOUNTAINS, 3);
        add(pool, TerrainType.DESERT, 1);
        return pool.toArray(new TerrainType[0]);
    }

    private static void add(List<TerrainType> pool, TerrainType type, int count) {
        for (int i = 0; i < count; i++) {
            pool.add(type);
        }
    }

    /** Corner i of the pointy-top hexagon at axial (q, r), rounded so shared corners match. */
    private static Point cornerPoint(int q, int r, int i) {
        double cx = Math.sqrt(3) * (q + r / 2.0);
        double cy = 1.5 * r;
        double angle = Math.toRadians(60 * i - 30);
        return new Point(Math.round((cx + Math.cos(angle)) * 1000), Math.round((cy + Math.sin(angle)) * 1000));
    }

    private static Vertex vertexAt(Point p, Map<Point, Vertex> byPoint, List<Vertex> vertices) {
        Vertex existing = byPoint.get(p);
        if (existing != null) {
            return existing;
        }
        Vertex created = new Vertex(vertices.size());
        byPoint.put(p, created);
        vertices.add(created);
        return created;
    }

    private static Edge edgeBetween(Vertex a, Vertex b, Map<Long, Edge> byPair, List<Edge> edges) {
        long key = (long) Math.min(a.getId(), b.getId()) * 1000 + Math.max(a.getId(), b.getId());
        Edge existing = byPair.get(key);
        if (existing != null) {
            return existing;
        }
        Edge created = new Edge(edges.size(), a, b);
        byPair.put(key, created);
        edges.add(created);
        return created;
    }
}
