package catan.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * Factory pattern: builds a ready-to-play {@link Board}: 19 tiles, 54 vertices and 72 edges with
 * all adjacency wired up.
 *
 * <p>This class only does the geometry. What goes on each tile is decided by a
 * {@link BoardLayoutStrategy} (or given directly as a {@link BoardLayout}), so the two jobs can
 * change independently.
 */
public class BoardFactory {

    private record Point(long x, long y) {}

    /**
     * A random board: desert in the centre, other terrain and tokens shuffled, no other rule.
     * Same signature as before, so existing callers do not change.
     */
    public static Board createStandardBoard(Random random) {
        return createBoard(new RandomLayoutStrategy(), random);
    }

    /** A standard board whose terrain and tokens are chosen by the strategy. */
    public static Board createBoard(BoardLayoutStrategy strategy, Random random) {
        Objects.requireNonNull(strategy, "strategy");
        return createBoard(strategy.createLayout(random));
    }

    /** A standard board with exactly this layout. Tile i gets {@code layout} entry i. */
    public static Board createBoard(BoardLayout layout) {
        Objects.requireNonNull(layout, "layout");
        List<HexCoordinate> coords = HexCoordinate.standardBoard();
        if (layout.size() != coords.size()) {
            throw new IllegalArgumentException("A standard board needs " + coords.size()
                    + " tiles, the layout has " + layout.size());
        }

        List<Tile> tiles = new ArrayList<>();
        List<Vertex> vertices = new ArrayList<>();
        List<Edge> edges = new ArrayList<>();
        Map<Point, Vertex> vertexByPoint = new HashMap<>();
        Map<Long, Edge> edgeByVertexPair = new HashMap<>();

        for (int tileId = 0; tileId < coords.size(); tileId++) {
            HexCoordinate c = coords.get(tileId);
            Tile tile = new Tile(tileId, layout.terrainAt(tileId), layout.numberTokenAt(tileId));

            Vertex[] corners = new Vertex[6];
            for (int i = 0; i < 6; i++) {
                corners[i] = vertexAt(cornerPoint(c, i), vertexByPoint, vertices);
                tile.addVertex(corners[i]);
            }
            for (int i = 0; i < 6; i++) {
                tile.addEdge(edgeBetween(corners[i], corners[(i + 1) % 6], edgeByVertexPair, edges));
            }
            tiles.add(tile);
        }
        return new Board(tiles, vertices, edges);
    }

    /** Corner i of the pointy-top hexagon at c, rounded so corners shared by tiles match. */
    private static Point cornerPoint(HexCoordinate c, int i) {
        double cx = Math.sqrt(3) * (c.q() + c.r() / 2.0);
        double cy = 1.5 * c.r();
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
