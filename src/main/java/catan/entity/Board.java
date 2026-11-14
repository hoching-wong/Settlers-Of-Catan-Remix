package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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

    // ---- Placement rules ----

    private boolean onBoard(Vertex vertex) {
        return verticesById.get(vertex.getId()) == vertex;
    }

    private boolean onBoard(Edge edge) {
        return edgesById.get(edge.getId()) == edge;
    }

    private static boolean ownsRoadAt(Edge edge, Player player) {
        return edge.isOccupied() && edge.getRoad().getOwner() == player;
    }

    /**
     * Whether the player may put a settlement on the vertex. The vertex must be on this board,
     * empty, and at least 2 edges from every settlement and city (whoever owns it). Outside
     * setup it must also touch one of the player's roads.
     */
    public boolean isValidSettlementLocation(Vertex vertex, Player player, boolean isSetup) {
        Objects.requireNonNull(vertex, "vertex");
        Objects.requireNonNull(player, "player");
        if (!onBoard(vertex) || vertex.isOccupied()) {
            return false;
        }
        for (Vertex neighbour : vertex.getAdjacentVertices()) {
            if (neighbour.isOccupied()) {
                return false;
            }
        }
        if (isSetup) {
            return true;
        }
        for (Edge edge : vertex.getAdjacentEdges()) {
            if (ownsRoadAt(edge, player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the player may put a road on the edge. The edge must be on this board and empty.
     * During setup {@code setupAnchor} is the settlement just placed and the road must touch it.
     * Otherwise (anchor null) the road must connect to one of the player's buildings or roads,
     * and an opponent's settlement or city blocks the connection through that vertex.
     */
    public boolean isValidRoadLocation(Edge edge, Player player, Vertex setupAnchor) {
        Objects.requireNonNull(edge, "edge");
        Objects.requireNonNull(player, "player");
        if (!onBoard(edge) || edge.isOccupied()) {
            return false;
        }
        if (setupAnchor != null) {
            return edge.getVertex1() == setupAnchor || edge.getVertex2() == setupAnchor;
        }
        return connectsThrough(edge, edge.getVertex1(), player)
                || connectsThrough(edge, edge.getVertex2(), player);
    }

    private static boolean connectsThrough(Edge edge, Vertex vertex, Player player) {
        if (vertex.isOccupied()) {
            return vertex.getBuilding().getOwner() == player;
        }
        for (Edge other : vertex.getAdjacentEdges()) {
            if (other != edge && ownsRoadAt(other, player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts a settlement on an empty vertex and adds it to its owner's buildings. This checks
     * only that the placement is consistent, not the game rules: call
     * {@link #isValidSettlementLocation} first. A city cannot be placed directly, only through
     * {@link #upgradeToCity}.
     */
    public void placeBuilding(Vertex vertex, Building building) {
        Objects.requireNonNull(vertex, "vertex");
        Objects.requireNonNull(building, "building");
        if (!onBoard(vertex)) {
            throw new IllegalArgumentException("Vertex " + vertex.getId() + " is not on this board");
        }
        if (building.getLocation() != vertex) {
            throw new IllegalArgumentException("The building's location is not the given vertex");
        }
        if (building instanceof City) {
            throw new IllegalArgumentException("A city can only be built by upgrading a settlement");
        }
        if (vertex.isOccupied()) {
            throw new IllegalArgumentException("Vertex " + vertex.getId() + " is already occupied");
        }
        vertex.setBuilding(building);
        building.getOwner().addBuilding(building);
    }

    /**
     * Replaces the city owner's own settlement on the vertex with the city, in both the vertex
     * and the owner's buildings.
     */
    public void upgradeToCity(Vertex vertex, City city) {
        Objects.requireNonNull(vertex, "vertex");
        Objects.requireNonNull(city, "city");
        if (!onBoard(vertex)) {
            throw new IllegalArgumentException("Vertex " + vertex.getId() + " is not on this board");
        }
        if (city.getLocation() != vertex) {
            throw new IllegalArgumentException("The city's location is not the given vertex");
        }
        Building existing = vertex.getBuilding();
        if (!(existing instanceof Settlement) || existing.getOwner() != city.getOwner()) {
            throw new IllegalArgumentException("A city can only replace the owner's own settlement");
        }
        Player owner = city.getOwner();
        owner.removeBuilding(existing);
        owner.addBuilding(city);
        vertex.setBuilding(city);
    }

    /** Puts a road on an empty edge and adds it to its owner's roads. Call {@link #isValidRoadLocation} first. */
    public void placeRoad(Edge edge, Road road) {
        Objects.requireNonNull(edge, "edge");
        Objects.requireNonNull(road, "road");
        if (!onBoard(edge)) {
            throw new IllegalArgumentException("Edge " + edge.getId() + " is not on this board");
        }
        if (road.getLocation() != edge) {
            throw new IllegalArgumentException("The road's location is not the given edge");
        }
        if (edge.isOccupied()) {
            throw new IllegalArgumentException("Edge " + edge.getId() + " is already occupied");
        }
        edge.setRoad(road);
        road.getOwner().addRoad(road);
    }
}
