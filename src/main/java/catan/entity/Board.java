package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private final List<Tile> tiles;
    private final List<Vertex> vertices;
    private final List<Edge> edges;
    private final Map<Integer, Tile> tilesById = new HashMap<>();
    private final Map<Integer, Vertex> verticesById = new HashMap<>();
    private final Map<Integer, Edge> edgesById = new HashMap<>();

    public Board(List<Tile> tiles, List<Vertex> vertices, List<Edge> edges) {
        this.tiles = List.copyOf(tiles);
        this.vertices = List.copyOf(vertices);
        this.edges = List.copyOf(edges);
        for (Tile t : this.tiles) {
            tilesById.put(t.getId(), t);
        }
        for (Vertex v : this.vertices) {
            verticesById.put(v.getId(), v);
        }
        for (Edge e : this.edges) {
            edgesById.put(e.getId(), e);
        }
    }

    public List<Tile> getTiles() {
        return tiles;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Tile getTile(int tileId) {
        return lookup(tilesById, tileId, "tile");
    }

    public Vertex getVertex(int vertexId) {
        return lookup(verticesById, vertexId, "vertex");
    }

    public Edge getEdge(int edgeId) {
        return lookup(edgesById, edgeId, "edge");
    }

    private static <T> T lookup(Map<Integer, T> map, int id, String what) {
        T found = map.get(id);
        if (found == null) {
            throw new IllegalArgumentException("No " + what + " with id " + id);
        }
        return found;
    }

    /** The tiles whose number token equals the dice total. A total of 7 matches none. */
    public List<Tile> getMatchingTiles(int diceTotal) {
        List<Tile> result = new ArrayList<>();
        for (Tile t : tiles) {
            if (t.hasNumberToken() && t.getNumberToken() == diceTotal) {
                result.add(t);
            }
        }
        return Collections.unmodifiableList(result);
    }

    /** The tiles that have the vertex as a corner (1 to 3 tiles). */
    public List<Tile> getTilesAdjacentTo(Vertex vertex) {
        List<Tile> result = new ArrayList<>();
        for (Tile t : tiles) {
            if (t.getVertices().contains(vertex)) {
                result.add(t);
            }
        }
        return Collections.unmodifiableList(result);
    }
}
