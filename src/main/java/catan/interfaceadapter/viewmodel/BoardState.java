package catan.interfaceadapter.viewmodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** What the board view shows right now. Presenters fill it in; the view only reads it. */
public class BoardState {
    /** {@link #getTargetKind()} values. */
    public static final String VERTEX = "VERTEX";
    public static final String EDGE = "EDGE";

    private List<TileView> tiles = new ArrayList<>();
    private List<VertexView> vertices = new ArrayList<>();
    private List<EdgeView> edges = new ArrayList<>();

    private String selectedStructure;
    private String targetKind;
    private Set<Integer> highlightedVertexIds = new HashSet<>();
    private Set<Integer> highlightedEdgeIds = new HashSet<>();

    private String message = "";
    private boolean error;

    public List<TileView> getTiles() {
        return tiles;
    }

    public void setTiles(List<TileView> tiles) {
        this.tiles = List.copyOf(tiles);
    }

    public List<VertexView> getVertices() {
        return vertices;
    }

    public void setVertices(List<VertexView> vertices) {
        this.vertices = List.copyOf(vertices);
    }

    public List<EdgeView> getEdges() {
        return edges;
    }

    public void setEdges(List<EdgeView> edges) {
        this.edges = List.copyOf(edges);
    }

    /** "ROAD", "SETTLEMENT", "CITY", or null when the player is not building. */
    public String getSelectedStructure() {
        return selectedStructure;
    }

    /** "VERTEX" or "EDGE" while building, otherwise null. */
    public String getTargetKind() {
        return targetKind;
    }

    public Set<Integer> getHighlightedVertexIds() {
        return highlightedVertexIds;
    }

    public Set<Integer> getHighlightedEdgeIds() {
        return highlightedEdgeIds;
    }

    /** Starts building: remembers the structure and which places to highlight. */
    public void select(String structure, String kind, Set<Integer> highlightedIds) {
        this.selectedStructure = structure;
        this.targetKind = kind;
        this.highlightedVertexIds = VERTEX.equals(kind) ? new HashSet<>(highlightedIds) : new HashSet<>();
        this.highlightedEdgeIds = EDGE.equals(kind) ? new HashSet<>(highlightedIds) : new HashSet<>();
    }

    /** Stops building and removes the highlights. */
    public void clearSelection() {
        selectedStructure = null;
        targetKind = null;
        highlightedVertexIds = new HashSet<>();
        highlightedEdgeIds = new HashSet<>();
    }

    public String getMessage() {
        return message;
    }

    public boolean isError() {
        return error;
    }

    public void setMessage(String message, boolean error) {
        this.message = message == null ? "" : message;
        this.error = error;
    }
}
