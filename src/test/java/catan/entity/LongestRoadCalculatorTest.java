package catan.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests for LongestRoadCalculator.
 */
class LongestRoadCalculatorTest {

    private final Player alice = new Player("Alice");
    private final Player bob = new Player("Bob");
    private final Player charlie = new Player("Charlie");

    private final Map<Integer, Vertex> vertices = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    /**
     * Returns an existing vertex or creates a new one.
     *
     * @param id the vertex ID
     * @return the vertex
     */
    private Vertex vertex(int id) {
        return vertices.computeIfAbsent(id, Vertex::new);
    }

    /**
     * Creates a road between two vertices.
     *
     * @param player the road owner
     * @param first the first vertex ID
     * @param second the second vertex ID
     */
    private void addRoad(Player player, int first, int second) {
        Edge edge = new Edge(edges.size(), vertex(first), vertex(second));
        edge.setRoad(new Road(player, edge));
        edges.add(edge);
    }

    /**
     * Connects consecutive vertices with one player's roads.
     *
     * @param player the road owner
     * @param ids the ordered vertex IDs
     */
    private void addPath(Player player, int... ids) {
        for (int i = 0; i < ids.length - 1; i++) {
            addRoad(player, ids[i], ids[i + 1]);
        }
    }

    /**
     * Creates a board using the test vertices and edges.
     *
     * @return the test board
     */
    private Board createBoard() {
        return new Board(
                List.of(),
                new ArrayList<>(vertices.values()),
                edges);
    }

    @Test
    void noRoadsReturnsZero() {
        assertEquals(
                0, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void straightRoadCountsAllSegments() {
        addPath(alice, 0, 1, 2, 3, 4, 5);

        assertEquals(
                5, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void disconnectedRoadsAreNotCombined() {
        addPath(alice, 0, 1, 2, 3);
        addPath(alice, 10, 11, 12);

        assertEquals(
                3, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void branchDoesNotCountEveryRoad() {
        addPath(alice, 0, 1, 2, 3);
        addPath(alice, 0, 4);
        addPath(alice, 0, 5, 6);

        assertEquals(
                5, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void loopCountsEveryRoadOnce() {
        addPath(alice, 0, 1, 2, 3, 4, 5, 0);

        assertEquals(
                6, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void opponentSettlementBlocksRoad() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        vertex(2).setBuilding(new Settlement(bob, vertex(2)));

        assertEquals(
                3, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void opponentCityBlocksRoad() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        vertex(2).setBuilding(new City(bob, vertex(2)));

        assertEquals(
                3, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void ownSettlementDoesNotBlockRoad() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        vertex(2).setBuilding(new Settlement(alice, vertex(2)));

        assertEquals(
                5, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void fewerThanFiveRoadsHasNoHolder() {
        addPath(alice, 0, 1, 2, 3, 4);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), null);

        assertNull(holder);
    }

    @Test
    void fiveRoadsQualifyForLongestRoad() {
        addPath(alice, 0, 1, 2, 3, 4, 5);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), null);

        assertSame(alice, holder);
    }

    @Test
    void longerRoadTakesTitleFromCurrentHolder() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        addPath(bob, 10, 11, 12, 13, 14, 15, 16);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), alice);

        assertSame(bob, holder);
    }

    @Test
    void currentHolderKeepsTitleWhenTied() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        addPath(bob, 10, 11, 12, 13, 14, 15);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), alice);

        assertSame(alice, holder);
    }

    @Test
    void tieWithoutCurrentHolderHasNoWinner() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        addPath(bob, 10, 11, 12, 13, 14, 15);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), null);

        assertNull(holder);
    }

    @Test
    void threeWayCompetitionSelectsLongestRoad() {
        addPath(alice, 0, 1, 2, 3, 4, 5);
        addPath(bob, 10, 11, 12, 13, 14, 15, 16);
        addPath(charlie, 20, 21, 22, 23, 24, 25);

        Player holder = LongestRoadCalculator.findHolder(
                List.of(alice, bob, charlie), createBoard(), null);

        assertSame(bob, holder);
    }

    @Test
    void calculatorDoesNotChangeVictoryPoints() {
        addPath(alice, 0, 1, 2, 3, 4, 5);

        LongestRoadCalculator.findHolder(
                List.of(alice, bob), createBoard(), null);

        assertEquals(0, alice.getVictoryPoints());
    }

    @Test
    void nullArgumentsThrowExceptions() {
        Board board = createBoard();

        assertThrows(
                NullPointerException.class,
                () -> LongestRoadCalculator.longestRoadOf(null, board));

        assertThrows(
                NullPointerException.class,
                () -> LongestRoadCalculator.longestRoadOf(alice, null));

        assertThrows(
                NullPointerException.class,
                () -> LongestRoadCalculator.findHolder(null, board, null));
    }

    @Test
    void loopWithBranchCountsAllRoadsOnce() {
        addPath(alice, 0, 1, 2, 3, 4, 5, 0);
        addPath(alice, 2, 6, 7);

        assertEquals(
                8, LongestRoadCalculator.longestRoadOf(alice, createBoard()));
    }

    @Test
    void blockedCurrentHolderLosesTitle() {
        addPath(alice, 0, 1, 2, 3, 4, 5, 6);
        addPath(bob, 10, 11, 12, 13, 14, 15);

        Board board = createBoard();

        assertSame(
                alice,
                LongestRoadCalculator.findHolder(
                        List.of(alice, bob, charlie), board, alice));

        vertex(3).setBuilding(
                new Settlement(charlie, vertex(3)));

        assertEquals(
                3, LongestRoadCalculator.longestRoadOf(alice, board));

        Player newHolder = LongestRoadCalculator.findHolder(
                List.of(alice, bob, charlie), board, alice);

        assertSame(bob, newHolder);
    }
}
