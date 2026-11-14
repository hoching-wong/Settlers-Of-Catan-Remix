package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A corner of the board where settlements and cities are placed. */
public class Vertex {
    private final int id;
    private final List<Edge> edges = new ArrayList<>();
    private Building building;

    public Vertex(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /** Called by {@link Edge}'s constructor to register itself. Duplicates are ignored. */
    void addEdge(Edge edge) {
        if (!edges.contains(edge)) {
            edges.add(edge);
        }
    }

    public boolean isOccupied() {
        return building != null;
    }

    public Building getBuilding() {
        return building;
    }

    public void setBuilding(Building building) {
        this.building = building;
    }

    /** The vertices one edge away. */
    public List<Vertex> getAdjacentVertices() {
        List<Vertex> result = new ArrayList<>();
        for (Edge edge : edges) {
            result.add(edge.getOtherVertex(this));
        }
        return Collections.unmodifiableList(result);
    }

    public List<Edge> getAdjacentEdges() {
        return Collections.unmodifiableList(edges);
    }
}
