package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PlayerTest {
    private final Player player = new Player("Ann");

    @Test
    void newPlayerIsEmpty() {
        assertEquals("Ann", player.getName());
        assertEquals(0, player.getResources().getTotalResources());
        assertTrue(player.getBuildings().isEmpty());
        assertTrue(player.getRoads().isEmpty());
        assertFalse(player.hasLongestRoad());
        assertEquals(0, player.getVictoryPoints());
    }

    @Test
    void nullNameRejected() {
        assertThrows(NullPointerException.class, () -> new Player(null));
    }

    @Test
    void victoryPointsComeFromBuildings() {
        player.addBuilding(new Settlement(player, new Vertex(0)));
        player.addBuilding(new Settlement(player, new Vertex(1)));
        player.addBuilding(new City(player, new Vertex(2)));
        assertEquals(4, player.getVictoryPoints());
    }

    @Test
    void upgradingSettlementToCityAddsOnePoint() {
        Vertex v = new Vertex(0);
        Building settlement = new Settlement(player, v);
        player.addBuilding(settlement);
        assertEquals(1, player.getVictoryPoints());

        player.removeBuilding(settlement);
        player.addBuilding(new City(player, v));
        assertEquals(2, player.getVictoryPoints());
    }

    @Test
    void longestRoadAddsTwoAndIsRevocable() {
        player.addBuilding(new Settlement(player, new Vertex(0)));
        player.setHasLongestRoad(true);
        assertTrue(player.hasLongestRoad());
        assertEquals(3, player.getVictoryPoints());
        player.setHasLongestRoad(false);
        assertEquals(1, player.getVictoryPoints());
    }

    @Test
    void roadsDoNotScore() {
        player.addRoad(new Road(player, new Edge(0, new Vertex(0), new Vertex(1))));
        assertEquals(1, player.getRoads().size());
        assertEquals(0, player.getVictoryPoints());
    }

    @Test
    void removingAbsentBuildingIsNoOp() {
        player.removeBuilding(new Settlement(player, new Vertex(0)));
        assertTrue(player.getBuildings().isEmpty());
    }

    @Test
    void listsAreUnmodifiableViews() {
        assertThrows(UnsupportedOperationException.class,
                () -> player.getBuildings().add(new Settlement(player, new Vertex(0))));
        assertThrows(UnsupportedOperationException.class, () -> player.getRoads().clear());
    }

    @Test
    void nullBuildingOrRoadRejected() {
        assertThrows(NullPointerException.class, () -> player.addBuilding(null));
        assertThrows(NullPointerException.class, () -> player.addRoad(null));
    }
}
