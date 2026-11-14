package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class VertexTest {
    @Test
    void startsUnoccupied() {
        Vertex v = new Vertex(3);
        assertEquals(3, v.getId());
        assertFalse(v.isOccupied());
        assertNull(v.getBuilding());
        assertTrue(v.getAdjacentEdges().isEmpty());
        assertTrue(v.getAdjacentVertices().isEmpty());
    }

    @Test
    void setBuildingOccupiesAndClearing() {
        Vertex v = new Vertex(0);
        Building b = new Settlement(new Player("Ann"), v);
        v.setBuilding(b);
        assertTrue(v.isOccupied());
        assertSame(b, v.getBuilding());
        v.setBuilding(null);
        assertFalse(v.isOccupied());
    }

    @Test
    void edgesRegisterWithBothVerticesAndGiveNeighbours() {
        Vertex a = new Vertex(0);
        Vertex b = new Vertex(1);
        Vertex c = new Vertex(2);
        Edge ab = new Edge(0, a, b);
        Edge bc = new Edge(1, b, c);

        assertEquals(List.of(ab), a.getAdjacentEdges());
        assertEquals(List.of(ab, bc), b.getAdjacentEdges());
        assertEquals(List.of(b), a.getAdjacentVertices());
        assertEquals(List.of(a, c), b.getAdjacentVertices());
        assertEquals(List.of(b), c.getAdjacentVertices());
    }

    @Test
    void adjacencyListsCannotBeModified() {
        Vertex a = new Vertex(0);
        new Edge(0, a, new Vertex(1));
        assertThrows(UnsupportedOperationException.class, () -> a.getAdjacentEdges().clear());
        assertThrows(UnsupportedOperationException.class, () -> a.getAdjacentVertices().clear());
    }
}
