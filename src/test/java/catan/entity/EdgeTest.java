package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.Test;

class EdgeTest {
    @Test
    void gettersAndOtherVertex() {
        Vertex a = new Vertex(0);
        Vertex b = new Vertex(1);
        Edge e = new Edge(7, a, b);
        assertEquals(7, e.getId());
        assertSame(a, e.getVertex1());
        assertSame(b, e.getVertex2());
        assertSame(b, e.getOtherVertex(a));
        assertSame(a, e.getOtherVertex(b));
    }

    @Test
    void otherVertexRejectsVertexNotOnEdge() {
        Edge e = new Edge(0, new Vertex(0), new Vertex(1));
        assertThrows(IllegalArgumentException.class, () -> e.getOtherVertex(new Vertex(2)));
    }

    @Test
    void sameVertexTwiceOrNullRejected() {
        Vertex a = new Vertex(0);
        assertThrows(IllegalArgumentException.class, () -> new Edge(0, a, a));
        assertThrows(NullPointerException.class, () -> new Edge(0, null, a));
        assertThrows(NullPointerException.class, () -> new Edge(0, a, null));
    }

    @Test
    void roadOccupiesEdge() {
        Edge e = new Edge(0, new Vertex(0), new Vertex(1));
        assertFalse(e.isOccupied());
        assertNull(e.getRoad());
        Road r = new Road(new Player("Ann"), e);
        e.setRoad(r);
        assertTrue(e.isOccupied());
        assertSame(r, e.getRoad());
    }

    @Test
    void adjacentEdgesShareAVertexAndExcludeSelf() {
        Vertex a = new Vertex(0);
        Vertex b = new Vertex(1);
        Vertex c = new Vertex(2);
        Vertex d = new Vertex(3);
        Vertex far = new Vertex(4);
        Edge ab = new Edge(0, a, b);
        Edge bc = new Edge(1, b, c);
        Edge bd = new Edge(2, b, d);
        Edge cFar = new Edge(3, c, far);

        assertEquals(List.of(bc, bd), ab.getAdjacentEdges());
        assertEquals(List.of(ab, bd, cFar), bc.getAdjacentEdges());
        assertFalse(ab.getAdjacentEdges().contains(cFar));
        assertFalse(ab.getAdjacentEdges().contains(ab));
    }
}
