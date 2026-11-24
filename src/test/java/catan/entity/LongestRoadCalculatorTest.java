package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class LongestRoadCalculatorTest {
    private final Player ann = new Player("Ann");
    private final Player bob = new Player("Bob");
    private final Player cat = new Player("Cat");

    // A small hand-made graph so every shape can be drawn exactly.
    private final Map<Integer, Vertex> vertices = new HashMap<>();
    private final Map<String, Edge> edges = new HashMap<>();
    private final List<Edge> edgeList = new ArrayList<>();

    private Vertex v(int id) {
        return vertices.computeIfAbsent(id, Vertex::new);
    }

    private Edge link(int a, int b) {
        String key = Math.min(a, b) + "-" + Math.max(a, b);
        return edges.computeIfAbsent(key, k -> {
            Edge e = new Edge(edgeList.size(), v(a), v(b));
            edgeList.add(e);
            return e;
        });
    }

    /** Roads along consecutive vertex ids, for example path(ann, 0, 1, 2) is two roads. */
    private void path(Player p, int... ids) {
        for (int i = 0; i + 1 < ids.length; i++) {
            Edge e = link(ids[i], ids[i + 1]);
            e.setRoad(new Road(p, e));
        }
    }

    private void settle(Player p, int id) {
        v(id).setBuilding(new Settlement(p, v(id)));
    }

    private void city(Player p, int id) {
        v(id).setBuilding(new City(p, v(id)));
    }

    private Board board() {
        return new Board(List.of(), new ArrayList<>(vertices.values()), edgeList);
    }

    private int longest(Player p) {
        return LongestRoadCalculator.longestRoadOf(p, board());
    }

    private Player holder(Player current, Player... players) {
        return LongestRoadCalculator.findHolder(List.of(players), board(), current);
    }

    // ---- longestRoadOf: basic shapes ----

    @Test
    void noRoadsIsZero() {
        link(0, 1);
        assertEquals(0, longest(ann));
    }

    @Test
    void emptyBoardIsZero() {
        assertEquals(0, longest(ann));
    }

    @Test
    void singleRoadIsOne() {
        path(ann, 0, 1);
        assertEquals(1, longest(ann));
    }

    @Test
    void straightLinesCountEverySegment() {
        for (int n = 1; n <= 8; n++) {
            LongestRoadCalculatorTest fresh = new LongestRoadCalculatorTest();
            int[] ids = new int[n + 1];
            for (int i = 0; i <= n; i++) {
                ids[i] = i;
            }
            fresh.path(fresh.ann, ids);
            assertEquals(n, fresh.longest(fresh.ann), "line of " + n);
        }
    }

    @Test
    void disconnectedPiecesDoNotAdd() {
        path(ann, 0, 1, 2, 3);
        path(ann, 10, 11, 12);
        assertEquals(3, longest(ann));
    }

    @Test
    void roadsThatOnlyMeetAtNoSharedVertexAreSeparate() {
        path(ann, 0, 1);
        path(ann, 2, 3);
        assertEquals(1, longest(ann));
    }

    // ---- branches ----

    @Test
    void branchTakesTheTwoLongestArms() {
        // centre 0 with arms of length 3, 1 and 2
        path(ann, 0, 1, 2, 3);
        path(ann, 0, 4);
        path(ann, 0, 5, 6);
        assertEquals(5, longest(ann));
    }

    @Test
    void branchWithEqualArms() {
        path(ann, 0, 1, 2);
        path(ann, 0, 3, 4);
        path(ann, 0, 5, 6);
        assertEquals(4, longest(ann));
    }

    @Test
    void branchOffTheMiddleOfALine() {
        path(ann, 0, 1, 2, 3, 4); // line of 4
        path(ann, 2, 5); // short spur at the middle
        assertEquals(4, longest(ann));
        path(ann, 5, 6, 7); // spur grows to 3
        assertEquals(5, longest(ann)); // spur (3) plus half of the line (2)
    }

    // ---- loops ----

    @Test
    void loopCountsEverySegment() {
        path(ann, 0, 1, 2, 3, 4, 5, 0);
        assertEquals(6, longest(ann));
    }

    @Test
    void triangleIsThree() {
        path(ann, 0, 1, 2, 0);
        assertEquals(3, longest(ann));
    }

    @Test
    void loopWithATailUsesBoth() {
        path(ann, 0, 1, 2, 3, 0); // loop of 4
        path(ann, 0, 4, 5); // tail of 2
        assertEquals(6, longest(ann));
    }

    @Test
    void figureEightUsesBothLoops() {
        path(ann, 0, 1, 2, 0); // triangle
        path(ann, 0, 3, 4, 0); // second triangle sharing vertex 0
        assertEquals(6, longest(ann));
    }

    @Test
    void twoLoopsJoinedByABridge() {
        path(ann, 0, 1, 2, 0);
        path(ann, 2, 3);
        path(ann, 3, 4, 5, 3);
        assertEquals(7, longest(ann));
    }

    @Test
    void loopWithTwoTailsAtDifferentVertices() {
        path(ann, 0, 1, 2, 3, 0); // loop of 4
        path(ann, 0, 4, 5); // tail at 0
        path(ann, 2, 6, 7); // tail at 2 (opposite corner)
        // best road: one tail (2), two sides of the loop to the opposite corner (2), other tail (2)
        assertEquals(6, longest(ann));
    }

    // ---- breaks by other players ----

    @Test
    void opponentSettlementInTheMiddleSplitsALine() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 2);
        assertEquals(3, longest(ann));
    }

    @Test
    void opponentCityBreaksToo() {
        path(ann, 0, 1, 2, 3, 4, 5);
        city(bob, 2);
        assertEquals(3, longest(ann));
    }

    @Test
    void ownSettlementOrCityNeverBreaksARoad() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(ann, 2);
        city(ann, 3);
        assertEquals(5, longest(ann));
    }

    @Test
    void opponentBuildingAtTheEndOfALineChangesNothing() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 0);
        settle(bob, 5);
        assertEquals(5, longest(ann));
    }

    @Test
    void opponentBuildingNextToTheEndCutsOffOneSegment() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 1);
        assertEquals(4, longest(ann)); // 1-2-3-4-5, with 0-1 left over
    }

    @Test
    void breakAtTheCentreOfABranchLeavesTheLongestArm() {
        path(ann, 0, 1, 2, 3);
        path(ann, 0, 4);
        path(ann, 0, 5, 6);
        settle(bob, 0);
        assertEquals(3, longest(ann));
    }

    @Test
    void breakInsideALoopTurnsItIntoAChain() {
        path(ann, 0, 1, 2, 3, 4, 5, 0);
        settle(bob, 0);
        // the road runs from the settlement all the way round to it again
        assertEquals(6, longest(ann));
    }

    @Test
    void breakInsideALoopAwayFromTheTail() {
        path(ann, 0, 1, 2, 3, 0); // loop of 4
        path(ann, 0, 4, 5); // tail
        settle(bob, 2); // opposite corner from the tail junction
        // Going all the way round would pass through Bob's settlement, so the best road is the
        // tail (2) plus one side of the loop up to the settlement (2).
        assertEquals(4, longest(ann));
    }

    @Test
    void breakAtTheJunctionOfALoopAndATail() {
        path(ann, 0, 1, 2, 3, 0); // loop of 4
        path(ann, 0, 4, 5); // tail
        settle(bob, 0);
        // the tail and the loop can no longer be joined through vertex 0
        assertEquals(4, longest(ann));
    }

    @Test
    void breaksAreIndependentPerPlayer() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        settle(bob, 2); // breaks Ann's line, not Bob's own
        assertEquals(3, longest(ann));
        assertEquals(5, longest(bob));
    }

    // ---- other players' roads ----

    @Test
    void opponentRoadsAreNotCounted() {
        path(ann, 0, 1);
        path(bob, 1, 2);
        path(ann, 2, 3);
        assertEquals(1, longest(ann));
        assertEquals(1, longest(bob));
    }

    @Test
    void anOpponentRoadDoesNotBlockByItself() {
        path(ann, 0, 1, 2, 3);
        path(bob, 2, 4); // a bob road leaving a shared vertex
        assertEquals(3, longest(ann));
    }

    // ---- purity ----

    @Test
    void calculatingChangesNothing() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 2);
        longest(ann);
        longest(bob);
        holder(null, ann, bob);
        assertEquals(5, edgeList.stream().filter(Edge::isOccupied).count());
        assertFalse(ann.hasLongestRoad());
        assertFalse(bob.hasLongestRoad());
        assertTrue(ann.getRoads().isEmpty(), "player lists are not touched");
        assertEquals(0, ann.getVictoryPoints());
    }

    @Test
    void argumentsMustNotBeNull() {
        assertThrows(NullPointerException.class, () -> LongestRoadCalculator.longestRoadOf(null, board()));
        assertThrows(NullPointerException.class, () -> LongestRoadCalculator.longestRoadOf(ann, null));
        assertThrows(NullPointerException.class,
                () -> LongestRoadCalculator.findHolder(null, board(), null));
        assertThrows(NullPointerException.class,
                () -> LongestRoadCalculator.findHolder(List.of(ann), null, null));
    }

    // ---- real board ----

    @Test
    void roadAroundOneHexOnTheStandardBoard() {
        Board standard = BoardFactory.createStandardBoard(new Random(5));
        Tile centre = standard.getTiles().get(9);
        for (Edge e : centre.getEdges()) {
            standard.placeRoad(e, new Road(ann, e));
        }
        assertEquals(6, LongestRoadCalculator.longestRoadOf(ann, standard));
    }

    @Test
    void fiveSidesOfAHexAreFiveAndGetTheTitle() {
        Board standard = BoardFactory.createStandardBoard(new Random(5));
        Tile centre = standard.getTiles().get(9);
        for (int i = 0; i < 5; i++) {
            Edge e = centre.getEdges().get(i);
            standard.placeRoad(e, new Road(ann, e));
        }
        assertEquals(5, LongestRoadCalculator.longestRoadOf(ann, standard));
        assertSame(ann, LongestRoadCalculator.findHolder(List.of(ann, bob), standard, null));
    }

    @Test
    void realSettlementBreaksARealRoad() {
        Board standard = BoardFactory.createStandardBoard(new Random(5));
        Tile centre = standard.getTiles().get(9);
        for (int i = 0; i < 5; i++) {
            Edge e = centre.getEdges().get(i);
            standard.placeRoad(e, new Road(ann, e));
        }
        // the vertex between sides 1 and 2 of the hex
        Vertex middle = centre.getEdges().get(1).getVertex1() == centre.getEdges().get(2).getVertex1()
                || centre.getEdges().get(1).getVertex1() == centre.getEdges().get(2).getVertex2()
                ? centre.getEdges().get(1).getVertex1()
                : centre.getEdges().get(1).getVertex2();
        standard.placeBuilding(middle, new Settlement(bob, middle));
        assertTrue(LongestRoadCalculator.longestRoadOf(ann, standard) < 5);
        assertNull(LongestRoadCalculator.findHolder(List.of(ann, bob), standard, ann));
    }

    @Test
    void largeDenseNetworkFinishesQuickly() {
        Board standard = BoardFactory.createStandardBoard(new Random(5));
        // the centre hex and the six around it: 30 roads in a honeycomb
        List<Edge> owned = new ArrayList<>();
        for (Tile t : standard.getTiles()) {
            if (t.getId() == 9 || sharesVertexWith(standard.getTile(9), t)) {
                for (Edge e : t.getEdges()) {
                    if (!owned.contains(e)) {
                        owned.add(e);
                    }
                }
            }
        }
        for (Edge e : owned) {
            standard.placeRoad(e, new Road(ann, e));
        }
        assertTimeoutPreemptively(Duration.ofSeconds(20), () -> {
            int length = LongestRoadCalculator.longestRoadOf(ann, standard);
            assertTrue(length >= 5 && length <= owned.size());
        });
    }

    private static boolean sharesVertexWith(Tile a, Tile b) {
        for (Vertex v : a.getVertices()) {
            if (b.getVertices().contains(v)) {
                return true;
            }
        }
        return false;
    }

    // ---- findHolder ----

    @Test
    void nobodyHoldsWithNoRoads() {
        assertNull(holder(null, ann, bob));
    }

    @Test
    void emptyPlayerListGivesNull() {
        path(ann, 0, 1, 2, 3, 4, 5);
        assertNull(LongestRoadCalculator.findHolder(List.of(), board(), null));
    }

    @Test
    void fourSegmentsDoNotQualify() {
        path(ann, 0, 1, 2, 3, 4);
        assertNull(holder(null, ann, bob));
    }

    @Test
    void fiveSegmentsQualify() {
        path(ann, 0, 1, 2, 3, 4, 5);
        assertSame(ann, holder(null, ann, bob));
    }

    @Test
    void longerRoadThanEveryoneWins() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15, 16);
        assertSame(bob, holder(null, ann, bob));
    }

    @Test
    void tieKeepsTheCurrentHolder() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        assertSame(ann, holder(ann, ann, bob));
        assertSame(bob, holder(bob, ann, bob));
    }

    @Test
    void tieKeepsTheHolderWhoeverIsListedFirst() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        assertSame(bob, LongestRoadCalculator.findHolder(List.of(ann, bob), board(), bob));
        assertSame(bob, LongestRoadCalculator.findHolder(List.of(bob, ann), board(), bob));
    }

    @Test
    void holderKeepsTheTitleWhenLonger() {
        path(ann, 0, 1, 2, 3, 4, 5, 6);
        path(bob, 10, 11, 12, 13, 14, 15);
        assertSame(ann, holder(ann, ann, bob));
    }

    @Test
    void holderLosesTheTitleToALongerRoad() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15, 16);
        assertSame(bob, holder(ann, ann, bob));
    }

    @Test
    void holderWhoFallsBelowFiveLosesTheTitleToNobody() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 2); // breaks Ann to 3
        path(bob, 10, 11, 12);
        assertNull(holder(ann, ann, bob));
    }

    @Test
    void brokenHolderHandsTheTitleToTheNextBestRoad() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(bob, 2); // breaks Ann to 3
        path(bob, 10, 11, 12, 13, 14, 15);
        assertSame(bob, holder(ann, ann, bob));
    }

    @Test
    void brokenHolderWithSeveralOthersTiedGivesNobodyTheTitle() {
        path(ann, 0, 1, 2, 3, 4, 5);
        settle(cat, 2); // breaks Ann to 3
        path(bob, 10, 11, 12, 13, 14, 15);
        path(cat, 20, 21, 22, 23, 24, 25);
        assertNull(holder(ann, ann, bob, cat));
    }

    @Test
    void holderTiedWithSeveralOthersKeepsTheTitle() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        path(cat, 20, 21, 22, 23, 24, 25);
        assertSame(ann, holder(ann, ann, bob, cat));
        assertSame(cat, holder(cat, ann, bob, cat));
    }

    @Test
    void noHolderAndATieAtTheTopGivesNobodyTheTitle() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        assertNull(holder(null, ann, bob));
    }

    @Test
    void noHolderAndAUniqueLeaderTakesTheTitle() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15);
        path(cat, 20, 21, 22, 23, 24, 25, 26);
        assertSame(cat, holder(null, ann, bob, cat));
    }

    @Test
    void holderWhoIsNotTheLongestAndNotTiedLosesIt() {
        path(ann, 0, 1, 2, 3, 4, 5);
        path(bob, 10, 11, 12, 13, 14, 15, 16, 17);
        path(cat, 20, 21, 22, 23, 24, 25, 26);
        assertSame(bob, holder(cat, ann, bob, cat));
    }

    @Test
    void breakingTheLongestRoadCanHandTheTitleOver() {
        // Ann has 7, Bob has 5. Cat's settlement splits Ann's road to 3 and 3.
        path(ann, 0, 1, 2, 3, 4, 5, 6, 7);
        path(bob, 10, 11, 12, 13, 14, 15);
        assertSame(ann, holder(ann, ann, bob));
        settle(cat, 3);
        assertSame(bob, holder(ann, ann, bob, cat));
    }

    @Test
    void roadsOfPlayersNotInTheListAreIgnored() {
        path(cat, 0, 1, 2, 3, 4, 5, 6);
        path(ann, 10, 11, 12, 13, 14, 15);
        assertSame(ann, holder(null, ann, bob));
    }

    @Test
    void currentHolderOutsideTheListCannotKeepTheTitle() {
        path(ann, 0, 1, 2, 3, 4, 5);
        assertSame(ann, holder(cat, ann, bob));
    }

    @Test
    void loopQualifiesAsALongRoad() {
        path(ann, 0, 1, 2, 3, 4, 5, 0);
        assertSame(ann, holder(null, ann, bob));
    }
}
