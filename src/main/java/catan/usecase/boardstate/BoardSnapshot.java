package catan.usecase.boardstate;

import catan.entity.Board;
import catan.entity.Building;
import catan.entity.City;
import catan.entity.Edge;
import catan.entity.HexCoordinate;
import catan.entity.Player;
import catan.entity.Tile;
import catan.entity.Vertex;
import java.util.ArrayList;
import java.util.List;

/**
 * A read-only copy of everything on the board, in plain data, for output data objects.
 * Presenters get this instead of the {@link Board} entity, so nothing outside the use case
 * layer can change the board by accident.
 *
 * <p>Any use case that changes the board (build, initial placement, ...) can put a fresh
 * snapshot in its output data so the board view is redrawn.
 *
 * @param tiles    the 19 tiles in id order
 * @param vertices every vertex in id order
 * @param edges    every edge in id order
 */
public record BoardSnapshot(List<TileData> tiles, List<VertexData> vertices, List<EdgeData> edges) {

    /** Owner index used when a vertex or edge is empty. */
    public static final int NO_OWNER = -1;

    /** Building names used in {@link VertexData#building()}. */
    public static final String NONE = "NONE";
    public static final String SETTLEMENT = "SETTLEMENT";
    public static final String CITY = "CITY";

    /**
     * @param q               axial column of the tile ({@link HexCoordinate})
     * @param r               axial row of the tile
     * @param terrain         terrain name, for example "FOREST"
     * @param resource        resource name, or null for the desert
     * @param numberToken     2-12, or 0 for the desert
     * @param cornerVertexIds the 6 corner vertex ids in the tile's corner order
     */
    public record TileData(int id, int q, int r, String terrain, String resource, int numberToken,
                           List<Integer> cornerVertexIds) {}

    /** @param ownerIndex index of the owner in the game's player list, or {@link #NO_OWNER} */
    public record VertexData(int id, String building, int ownerIndex) {}

    /** @param ownerIndex index of the road's owner in the player list, or {@link #NO_OWNER} */
    public record EdgeData(int id, int vertex1Id, int vertex2Id, int ownerIndex) {}

    public BoardSnapshot {
        tiles = List.copyOf(tiles);
        vertices = List.copyOf(vertices);
        edges = List.copyOf(edges);
    }

    /**
     * Copies the board. {@code players} is the game's player list, used to turn owners into
     * indexes (so the view can colour them); pass an empty list before the game exists.
     */
    public static BoardSnapshot of(Board board, List<Player> players) {
        List<TileData> tiles = new ArrayList<>();
        for (Tile t : board.getTiles()) {
            HexCoordinate c = HexCoordinate.ofStandardTile(t.getId());
            List<Integer> corners = new ArrayList<>();
            for (Vertex v : t.getVertices()) {
                corners.add(v.getId());
            }
            String resource = t.getResourceType() == null ? null : t.getResourceType().name();
            tiles.add(new TileData(t.getId(), c.q(), c.r(), t.getTerrain().name(), resource,
                    t.getNumberToken(), corners));
        }

        List<VertexData> vertices = new ArrayList<>();
        for (Vertex v : board.getVertices()) {
            Building b = v.getBuilding();
            if (b == null) {
                vertices.add(new VertexData(v.getId(), NONE, NO_OWNER));
            } else {
                String kind = b instanceof City ? CITY : SETTLEMENT;
                vertices.add(new VertexData(v.getId(), kind, players.indexOf(b.getOwner())));
            }
        }

        List<EdgeData> edges = new ArrayList<>();
        for (Edge e : board.getEdges()) {
            int owner = e.isOccupied() ? players.indexOf(e.getRoad().getOwner()) : NO_OWNER;
            edges.add(new EdgeData(e.getId(), e.getVertex1().getId(), e.getVertex2().getId(), owner));
        }
        return new BoardSnapshot(tiles, vertices, edges);
    }
}
