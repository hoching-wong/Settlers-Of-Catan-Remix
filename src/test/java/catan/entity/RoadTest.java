package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoadTest {
    private final Player player = new Player("Ann");
    private final Edge edge = new Edge(0, new Vertex(0), new Vertex(1));

    @Test
    void gettersReturnConstructorValues() {
        Road road = new Road(player, edge);
        assertSame(player, road.getOwner());
        assertSame(edge, road.getLocation());
    }

    @Test
    void nullOwnerOrLocationRejected() {
        assertThrows(NullPointerException.class, () -> new Road(null, edge));
        assertThrows(NullPointerException.class, () -> new Road(player, null));
    }
}
