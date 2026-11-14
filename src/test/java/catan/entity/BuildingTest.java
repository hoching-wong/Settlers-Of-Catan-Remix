package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BuildingTest {
    private final Player player = new Player("Ann");
    private final Vertex vertex = new Vertex(0);

    @Test
    void settlementIsWorthOnePoint() {
        Building b = new Settlement(player, vertex);
        assertEquals(1, b.getVictoryPoints());
        assertSame(player, b.getOwner());
        assertSame(vertex, b.getLocation());
    }

    @Test
    void cityIsWorthTwoPoints() {
        Building b = new City(player, vertex);
        assertEquals(2, b.getVictoryPoints());
        assertSame(player, b.getOwner());
        assertSame(vertex, b.getLocation());
    }

    @Test
    void nullOwnerOrLocationRejected() {
        assertThrows(NullPointerException.class, () -> new Settlement(null, vertex));
        assertThrows(NullPointerException.class, () -> new Settlement(player, null));
        assertThrows(NullPointerException.class, () -> new City(null, vertex));
        assertThrows(NullPointerException.class, () -> new City(player, null));
    }
}
