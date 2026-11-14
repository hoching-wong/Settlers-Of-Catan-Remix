package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** A side of the board between two vertices where a road is placed. */
public class Edge {
    private final int id;
    private final Vertex vertex1;
    private final Vertex vertex2;
    private Road road;

    /** Creates the edge and registers it with both of its vertices. */
    public Edge(int id, Vertex vertex1, Vertex vertex2) {
        this.id = id;
        this.vertex1 = Objects.requireNonNull(vertex1, "vertex1");
        this.vertex2 = Objects.requireNonNull(vertex2, "vertex2");
        if (vertex1 == vertex2) {
            throw new IllegalArgumentException("An edge needs two different vertices");
        }
        vertex1.addEdge(this);
        vertex2.addEdge(this);
    }

    public int getId() {
        return id;
    }

    public Vertex getVertex1() {
        return vertex1;
    }

    public Vertex getVertex2() {
        return vertex2;
    }

    /** The endpoint that is not {@code vertex}. */
    public Vertex getOtherVertex(Vertex vertex) {
        if (vertex == vertex1) {
            return vertex2;
        }
        if (vertex == vertex2) {
            return vertex1;
        }
        throw new IllegalArgumentException("Vertex " + vertex.getId() + " is not on edge " + id);
    }

    public boolean isOccupied() {
        return road != null;
    }

    public Road getRoad() {
        return road;
    }

    public void setRoad(Road road) {
        this.road = road;
    }

    /** The edges that share a vertex with this one, not including this edge. */
    public List<Edge> getAdjacentEdges() {
        List<Edge> result = new ArrayList<>();
        for (Vertex v : List.of(vertex1, vertex2)) {
            for (Edge e : v.getAdjacentEdges()) {
                if (e != this) {
                    result.add(e);
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
}
