package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BoardPlacementTest {
    private final Board board = BoardFactory.createStandardBoard(new Random(1));
    private final Player ann = new Player("Ann");
    private final Player bob = new Player("Bob");

    /** Vertices a0-a1-a2-a3 along a path that never backtracks. */
    private final List<Vertex> path = new ArrayList<>();
    /** path.get(i) to path.get(i + 1). */
    private final List<Edge> pathEdges = new ArrayList<>();

    BoardPlacementTest() {
        Vertex current = board.getVertex(0);
        path.add(current);
        Edge previous = null;
        for (int i = 0; i < 5; i++) {
            Edge next = null;
            for (Edge e : current.getAdjacentEdges()) {
                if (e != previous) {
                    next = e;
                    break;
                }
            }
            pathEdges.add(next);
            current = next.getOtherVertex(current);
            path.add(current);
            previous = next;
        }
    }

    private void settle(Player p, Vertex v) {
        board.placeBuilding(v, new Settlement(p, v));
    }

    private void road(Player p, Edge e) {
        board.placeRoad(e, new Road(p, e));
    }

    // ---- isValidSettlementLocation ----

    @Test
    void anyEmptyVertexIsValidInSetupOnAnEmptyBoard() {
        for (Vertex v : board.getVertices()) {
            assertTrue(board.isValidSettlementLocation(v, ann, true));
        }
    }

    @Test
    void settlementNeedsARoadOutsideSetup() {
        for (Vertex v : board.getVertices()) {
            assertFalse(board.isValidSettlementLocation(v, ann, false));
        }
    }

    @Test
    void occupiedVertexIsInvalid() {
        settle(ann, path.get(0));
        assertFalse(board.isValidSettlementLocation(path.get(0), ann, true));
        assertFalse(board.isValidSettlementLocation(path.get(0), bob, true));
    }

    @Test
    void neighbouringVertexIsInvalidWhoeverOwnsTheBuilding() {
        settle(ann, path.get(1));
        assertFalse(board.isValidSettlementLocation(path.get(0), bob, true), "opponent's neighbour");
        assertFalse(board.isValidSettlementLocation(path.get(2), ann, true), "own neighbour");
    }

    @Test
    void vertexTwoEdgesAwayIsValid() {
        settle(ann, path.get(0));
        assertFalse(board.isValidSettlementLocation(path.get(1), bob, true));
        assertTrue(board.isValidSettlementLocation(path.get(2), bob, true));
    }

    @Test
    void cityBlocksNeighboursToo() {
        settle(ann, path.get(1));
        board.upgradeToCity(path.get(1), new City(ann, path.get(1)));
        assertFalse(board.isValidSettlementLocation(path.get(0), bob, true));
    }

    @Test
    void outsideSetupVertexMustTouchOwnRoad() {
        road(ann, pathEdges.get(0));
        assertTrue(board.isValidSettlementLocation(path.get(0), ann, false));
        assertTrue(board.isValidSettlementLocation(path.get(1), ann, false));
        assertFalse(board.isValidSettlementLocation(path.get(2), ann, false), "road does not reach it");
        assertFalse(board.isValidSettlementLocation(path.get(0), bob, false), "opponent's road");
    }

    @Test
    void outsideSetupStillObeysDistanceRule() {
        road(ann, pathEdges.get(0));
        road(ann, pathEdges.get(1));
        settle(bob, path.get(0));
        assertFalse(board.isValidSettlementLocation(path.get(1), ann, false), "next to Bob's settlement");
        assertTrue(board.isValidSettlementLocation(path.get(2), ann, false));
    }

    @Test
    void vertexFromAnotherBoardIsInvalid() {
        assertFalse(board.isValidSettlementLocation(new Vertex(0), ann, true));
    }

    @Test
    void settlementValidationRejectsNulls() {
        assertThrows(NullPointerException.class, () -> board.isValidSettlementLocation(null, ann, true));
        assertThrows(NullPointerException.class,
                () -> board.isValidSettlementLocation(path.get(0), null, true));
    }

    // ---- isValidRoadLocation ----

    @Test
    void setupRoadMustTouchTheAnchor() {
        settle(ann, path.get(1));
        assertTrue(board.isValidRoadLocation(pathEdges.get(0), ann, path.get(1)));
        assertTrue(board.isValidRoadLocation(pathEdges.get(1), ann, path.get(1)));
        assertFalse(board.isValidRoadLocation(pathEdges.get(2), ann, path.get(1)));
    }

    @Test
    void setupRoadCannotUseAnOccupiedEdge() {
        settle(ann, path.get(1));
        road(ann, pathEdges.get(0));
        assertFalse(board.isValidRoadLocation(pathEdges.get(0), ann, path.get(1)));
        assertTrue(board.isValidRoadLocation(pathEdges.get(1), ann, path.get(1)));
    }

    @Test
    void roadWithNoNetworkIsInvalidOutsideSetup() {
        for (Edge e : board.getEdges()) {
            assertFalse(board.isValidRoadLocation(e, ann, null));
        }
    }

    @Test
    void roadMayStartFromOwnBuilding() {
        settle(ann, path.get(1));
        assertTrue(board.isValidRoadLocation(pathEdges.get(0), ann, null));
        assertTrue(board.isValidRoadLocation(pathEdges.get(1), ann, null));
        assertFalse(board.isValidRoadLocation(pathEdges.get(2), ann, null));
        assertFalse(board.isValidRoadLocation(pathEdges.get(0), bob, null));
    }

    @Test
    void roadMayExtendOwnRoad() {
        settle(ann, path.get(0));
        road(ann, pathEdges.get(0));
        assertTrue(board.isValidRoadLocation(pathEdges.get(1), ann, null));
        assertFalse(board.isValidRoadLocation(pathEdges.get(2), ann, null));
    }

    @Test
    void opponentRoadDoesNotConnect() {
        road(bob, pathEdges.get(0));
        assertFalse(board.isValidRoadLocation(pathEdges.get(1), ann, null));
    }

    @Test
    void opponentSettlementBlocksTheRoadThroughIt() {
        settle(ann, path.get(0));
        road(ann, pathEdges.get(0));
        settle(bob, path.get(1));
        assertFalse(board.isValidRoadLocation(pathEdges.get(1), ann, null),
                "cannot continue through Bob's settlement");
    }

    @Test
    void occupiedEdgeIsInvalidOutsideSetup() {
        settle(ann, path.get(0));
        road(ann, pathEdges.get(0));
        assertFalse(board.isValidRoadLocation(pathEdges.get(0), ann, null));
        assertFalse(board.isValidRoadLocation(pathEdges.get(0), bob, null));
    }

    @Test
    void edgeFromAnotherBoardIsInvalid() {
        Edge foreign = new Edge(0, new Vertex(0), new Vertex(1));
        assertFalse(board.isValidRoadLocation(foreign, ann, null));
    }

    @Test
    void roadValidationRejectsNulls() {
        assertThrows(NullPointerException.class, () -> board.isValidRoadLocation(null, ann, null));
        assertThrows(NullPointerException.class, () -> board.isValidRoadLocation(pathEdges.get(0), null, null));
    }

    // ---- placeBuilding ----

    @Test
    void placeBuildingUpdatesVertexAndPlayer() {
        Vertex v = path.get(0);
        Settlement s = new Settlement(ann, v);
        board.placeBuilding(v, s);
        assertSame(s, v.getBuilding());
        assertTrue(v.isOccupied());
        assertEquals(List.of(s), ann.getBuildings());
        assertEquals(1, ann.getVictoryPoints());
    }

    @Test
    void placeBuildingRejectsOccupiedVertex() {
        settle(ann, path.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> board.placeBuilding(path.get(0), new Settlement(bob, path.get(0))));
        assertEquals(0, bob.getBuildings().size());
    }

    @Test
    void placeBuildingRejectsMismatchedLocation() {
        assertThrows(IllegalArgumentException.class,
                () -> board.placeBuilding(path.get(0), new Settlement(ann, path.get(1))));
        assertFalse(path.get(0).isOccupied());
        assertTrue(ann.getBuildings().isEmpty());
    }

    @Test
    void placeBuildingRejectsCityAndForeignVertex() {
        assertThrows(IllegalArgumentException.class,
                () -> board.placeBuilding(path.get(0), new City(ann, path.get(0))));
        Vertex foreign = new Vertex(0);
        assertThrows(IllegalArgumentException.class,
                () -> board.placeBuilding(foreign, new Settlement(ann, foreign)));
    }

    // ---- upgradeToCity ----

    @Test
    void upgradeReplacesSettlementAndAddsOnePoint() {
        Vertex v = path.get(0);
        settle(ann, v);
        City city = new City(ann, v);
        board.upgradeToCity(v, city);
        assertSame(city, v.getBuilding());
        assertEquals(List.of(city), ann.getBuildings());
        assertEquals(2, ann.getVictoryPoints());
    }

    @Test
    void upgradeRejectsEmptyVertex() {
        assertThrows(IllegalArgumentException.class,
                () -> board.upgradeToCity(path.get(0), new City(ann, path.get(0))));
    }

    @Test
    void upgradeRejectsOpponentsSettlement() {
        settle(bob, path.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> board.upgradeToCity(path.get(0), new City(ann, path.get(0))));
        assertEquals(1, bob.getVictoryPoints());
        assertTrue(ann.getBuildings().isEmpty());
    }

    @Test
    void upgradeRejectsExistingCity() {
        Vertex v = path.get(0);
        settle(ann, v);
        board.upgradeToCity(v, new City(ann, v));
        assertThrows(IllegalArgumentException.class, () -> board.upgradeToCity(v, new City(ann, v)));
        assertEquals(2, ann.getVictoryPoints());
    }

    @Test
    void upgradeRejectsMismatchedLocation() {
        settle(ann, path.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> board.upgradeToCity(path.get(0), new City(ann, path.get(1))));
    }

    // ---- placeRoad ----

    @Test
    void placeRoadUpdatesEdgeAndPlayer() {
        Edge e = pathEdges.get(0);
        Road r = new Road(ann, e);
        board.placeRoad(e, r);
        assertSame(r, e.getRoad());
        assertTrue(e.isOccupied());
        assertEquals(List.of(r), ann.getRoads());
        assertEquals(0, ann.getVictoryPoints());
    }

    @Test
    void placeRoadRejectsOccupiedMismatchedAndForeignEdges() {
        road(ann, pathEdges.get(0));
        assertThrows(IllegalArgumentException.class,
                () -> board.placeRoad(pathEdges.get(0), new Road(bob, pathEdges.get(0))));
        assertThrows(IllegalArgumentException.class,
                () -> board.placeRoad(pathEdges.get(1), new Road(bob, pathEdges.get(2))));
        Edge foreign = new Edge(0, new Vertex(0), new Vertex(1));
        assertThrows(IllegalArgumentException.class, () -> board.placeRoad(foreign, new Road(bob, foreign)));
        assertTrue(bob.getRoads().isEmpty());
    }

    // ---- snake-style scenario ----

    @Test
    void setupSequenceFollowsTheRules() {
        // Ann places a settlement and an adjacent road, then Bob places next door
        assertTrue(board.isValidSettlementLocation(path.get(0), ann, true));
        settle(ann, path.get(0));
        assertTrue(board.isValidRoadLocation(pathEdges.get(0), ann, path.get(0)));
        road(ann, pathEdges.get(0));

        assertFalse(board.isValidSettlementLocation(path.get(1), bob, true), "too close to Ann");
        assertTrue(board.isValidSettlementLocation(path.get(2), bob, true));

        // Ann can later build a settlement two edges from her first one along her road network
        road(ann, pathEdges.get(1));
        assertTrue(board.isValidSettlementLocation(path.get(2), ann, false));
    }
}
