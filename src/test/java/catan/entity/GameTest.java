package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameTest {
    private final Board board = BoardFactory.createStandardBoard(new Random(11));

    private List<Player> players(int n) {
        List<Player> list = new ArrayList<>();
        for (int i = 1; i <= n; i++) {
            list.add(new Player("P" + i));
        }
        return list;
    }

    private Game game(int n) {
        return new Game(board, players(n), WeatherData.none(), new Dice(new Random(1)));
    }

    /** Places a settlement then a road for the current player, driving the game through the rules. */
    private void placeSetupTurn(Game g) {
        Player p = g.getCurrentPlayer();
        Vertex spot = null;
        for (Vertex v : board.getVertices()) {
            if (board.isValidSettlementLocation(v, p, true)) {
                spot = v;
                break;
            }
        }
        assertNotNull(spot);
        board.placeBuilding(spot, new Settlement(p, spot));
        g.setLastPlacedSettlement(spot);
        g.advanceSetup();
        assertEquals(SetupStep.ROAD, g.getSetupStep());

        Edge road = null;
        for (Edge e : spot.getAdjacentEdges()) {
            if (board.isValidRoadLocation(e, p, g.getLastPlacedSettlement())) {
                road = e;
                break;
            }
        }
        assertNotNull(road);
        board.placeRoad(road, new Road(p, road));
        g.advanceSetup();
    }

    private Game gameInPlay(int n) {
        Game g = game(n);
        g.startGame();
        while (g.getPhase() == GamePhase.SETUP) {
            placeSetupTurn(g);
        }
        return g;
    }

    private static void giveCities(Player p, int count) {
        for (int i = 0; i < count; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
    }

    // ---- construction ----

    @Test
    void startsInSetupWithPlayerOneToPlaceASettlement() {
        Game g = game(3);
        assertEquals(GamePhase.SETUP, g.getPhase());
        assertEquals(SetupStep.SETTLEMENT, g.getSetupStep());
        assertEquals(0, g.getSetupIndex());
        assertSame(g.getPlayers().get(0), g.getCurrentPlayer());
        assertEquals(0, g.getCurrentPlayerIndex());
        assertFalse(g.hasRolled());
        assertFalse(g.canRoll());
        assertNull(g.getWinner());
        assertNull(g.getLongestRoadHolder());
        assertNull(g.getLastPlacedSettlement());
        assertSame(board, g.getBoard());
    }

    @Test
    void acceptsTwoToFourPlayers() {
        for (int n = 2; n <= 4; n++) {
            assertEquals(n, game(n).getPlayers().size());
        }
    }

    @Test
    void rejectsTooFewOrTooManyPlayers() {
        assertThrows(IllegalArgumentException.class, () -> game(0));
        assertThrows(IllegalArgumentException.class, () -> game(1));
        assertThrows(IllegalArgumentException.class, () -> game(5));
    }

    @Test
    void rejectsNullAndDuplicatePlayers() {
        Player a = new Player("A");
        Player b = new Player("B");
        assertThrows(IllegalArgumentException.class,
                () -> new Game(board, Arrays.asList(a, null), WeatherData.none(), new Dice(new Random())));
        assertThrows(IllegalArgumentException.class,
                () -> new Game(board, List.of(a, b, a), WeatherData.none(), new Dice(new Random())));
    }

    @Test
    void rejectsNullArguments() {
        Dice d = new Dice(new Random());
        assertThrows(NullPointerException.class, () -> new Game(null, players(2), WeatherData.none(), d));
        assertThrows(NullPointerException.class, () -> new Game(board, null, WeatherData.none(), d));
        assertThrows(NullPointerException.class, () -> new Game(board, players(2), null, d));
        assertThrows(NullPointerException.class, () -> new Game(board, players(2), WeatherData.none(), null));
    }

    @Test
    void playersListIsUnmodifiableAndIndependentOfTheInput() {
        List<Player> input = players(2);
        Game g = new Game(board, input, WeatherData.none(), new Dice(new Random()));
        input.add(new Player("late"));
        assertEquals(2, g.getPlayers().size());
        assertThrows(UnsupportedOperationException.class, () -> g.getPlayers().clear());
    }

    @Test
    void keepsWeatherAndDice() {
        Dice d = new Dice(new Random());
        WeatherData w = WeatherData.fromWmoCode(0);
        Game g = new Game(board, players(2), w, d);
        assertSame(w, g.getWeather());
        assertSame(d, g.getDice());
    }

    // ---- snake order ----

    private static void assertOrder(Game g, int... expectedOneBased) {
        List<Player> order = g.getSetupOrder();
        assertEquals(expectedOneBased.length, order.size());
        for (int i = 0; i < expectedOneBased.length; i++) {
            assertSame(g.getPlayers().get(expectedOneBased[i] - 1), order.get(i), "turn " + i);
        }
    }

    @Test
    void setupOrderIsSnakeForTwoThreeAndFourPlayers() {
        assertOrder(game(2), 1, 2, 2, 1);
        assertOrder(game(3), 1, 2, 3, 3, 2, 1);
        assertOrder(game(4), 1, 2, 3, 4, 4, 3, 2, 1);
    }

    @Test
    void currentPlayerFollowsTheSnakeOrderThroughSetup() {
        for (int n = 2; n <= 4; n++) {
            Game g = game(n);
            g.startGame();
            List<Player> expected = g.getSetupOrder();
            for (int turn = 0; turn < 2 * n; turn++) {
                assertSame(expected.get(turn), g.getCurrentPlayer(), n + " players, turn " + turn);
                assertEquals(turn, g.getSetupIndex());
                placeSetupTurn(g);
            }
        }
    }

    // ---- setup flow ----

    @Test
    void settlementStepThenRoadStepThenNextPlayer() {
        Game g = game(2);
        g.startGame();
        Player first = g.getCurrentPlayer();
        Vertex v = board.getVertex(0);
        board.placeBuilding(v, new Settlement(first, v));
        g.setLastPlacedSettlement(v);
        assertEquals(SetupStep.SETTLEMENT, g.getSetupStep());

        g.advanceSetup();
        assertEquals(SetupStep.ROAD, g.getSetupStep());
        assertSame(first, g.getCurrentPlayer());
        assertSame(v, g.getLastPlacedSettlement());
        assertEquals(0, g.getSetupIndex());

        g.advanceSetup();
        assertEquals(SetupStep.SETTLEMENT, g.getSetupStep());
        assertSame(g.getPlayers().get(1), g.getCurrentPlayer());
        assertNull(g.getLastPlacedSettlement());
        assertEquals(1, g.getSetupIndex());
    }

    @Test
    void cannotAdvanceFromSettlementStepWithoutASettlement() {
        Game g = game(2);
        assertThrows(IllegalStateException.class, g::advanceSetup);
        assertEquals(SetupStep.SETTLEMENT, g.getSetupStep());
    }

    @Test
    void lastPlacedSettlementMustBeTheCurrentPlayersBuilding() {
        Game g = game(2);
        Player other = g.getPlayers().get(1);
        Vertex empty = board.getVertex(0);
        assertThrows(IllegalArgumentException.class, () -> g.setLastPlacedSettlement(empty));

        Vertex theirs = board.getVertex(10);
        board.placeBuilding(theirs, new Settlement(other, theirs));
        assertThrows(IllegalArgumentException.class, () -> g.setLastPlacedSettlement(theirs));
        assertThrows(NullPointerException.class, () -> g.setLastPlacedSettlement(null));
        assertNull(g.getLastPlacedSettlement());
    }

    @Test
    void lastPlacedSettlementCannotBeSetDuringTheRoadStep() {
        Game g = game(2);
        Player p = g.getCurrentPlayer();
        Vertex v = board.getVertex(0);
        board.placeBuilding(v, new Settlement(p, v));
        g.setLastPlacedSettlement(v);
        g.advanceSetup();
        assertThrows(IllegalStateException.class, () -> g.setLastPlacedSettlement(v));
    }

    @Test
    void setupEndsWithPlayerOneAndEveryoneHasTwoSettlementsAndTwoRoads() {
        for (int n = 2; n <= 4; n++) {
            Board fresh = BoardFactory.createStandardBoard(new Random(n));
            List<Player> ps = players(n);
            Game g = new Game(fresh, ps, WeatherData.none(), new Dice(new Random(1)));
            g.startGame();
            for (int turn = 0; turn < 2 * n; turn++) {
                Player p = g.getCurrentPlayer();
                Vertex spot = fresh.getVertices().stream()
                        .filter(v -> fresh.isValidSettlementLocation(v, p, true)).findFirst().orElseThrow();
                fresh.placeBuilding(spot, new Settlement(p, spot));
                g.setLastPlacedSettlement(spot);
                g.advanceSetup();
                Edge e = spot.getAdjacentEdges().stream()
                        .filter(x -> fresh.isValidRoadLocation(x, p, g.getLastPlacedSettlement()))
                        .findFirst().orElseThrow();
                fresh.placeRoad(e, new Road(p, e));
                g.advanceSetup();
            }
            assertEquals(GamePhase.PLAYING, g.getPhase(), n + " players");
            assertSame(ps.get(0), g.getCurrentPlayer());
            assertNull(g.getSetupStep());
            assertEquals(2 * n, g.getSetupIndex());
            assertTrue(g.canRoll());
            for (Player p : ps) {
                assertEquals(2, p.getBuildings().size(), p.getName());
                assertEquals(2, p.getRoads().size(), p.getName());
                assertEquals(2, p.getVictoryPoints());
            }
        }
    }

    @Test
    void cannotAdvanceSetupOnceRegularPlayHasBegun() {
        Game g = gameInPlay(2);
        assertThrows(IllegalStateException.class, g::advanceSetup);
        assertThrows(IllegalStateException.class, () -> g.setLastPlacedSettlement(board.getVertex(0)));
    }

    @Test
    void startGameOnlyWorksAtTheBeginning() {
        Game g = game(2);
        g.startGame();
        g.startSetupPhase();
        assertEquals(0, g.getSetupIndex());

        Player p = g.getCurrentPlayer();
        Vertex v = board.getVertex(0);
        board.placeBuilding(v, new Settlement(p, v));
        g.setLastPlacedSettlement(v);
        assertThrows(IllegalStateException.class, g::startGame);
        g.advanceSetup();
        assertThrows(IllegalStateException.class, g::startGame);

        Game playing = gameInPlay(2);
        assertThrows(IllegalStateException.class, playing::startGame);
    }

    // ---- turns ----

    @Test
    void cannotRollOrEndTurnDuringSetup() {
        Game g = game(2);
        assertFalse(g.canRoll());
        assertThrows(IllegalStateException.class, g::markRolled);
        assertThrows(IllegalStateException.class, g::endTurn);
    }

    @Test
    void rollOncePerTurn() {
        Game g = gameInPlay(2);
        assertTrue(g.canRoll());
        assertFalse(g.hasRolled());
        g.markRolled();
        assertTrue(g.hasRolled());
        assertFalse(g.canRoll());
        assertThrows(IllegalStateException.class, g::markRolled);
    }

    @Test
    void mustRollBeforeEndingTheTurn() {
        Game g = gameInPlay(2);
        assertThrows(IllegalStateException.class, g::endTurn);
        assertSame(g.getPlayers().get(0), g.getCurrentPlayer());
    }

    @Test
    void turnsCycleInOrderAndResetTheRoll() {
        for (int n = 2; n <= 4; n++) {
            Game g = gameInPlay(n);
            for (int turn = 0; turn < 2 * n + 1; turn++) {
                assertSame(g.getPlayers().get(turn % n), g.getCurrentPlayer(), n + " players, turn " + turn);
                assertTrue(g.canRoll());
                g.markRolled();
                g.endTurn();
                assertFalse(g.hasRolled());
            }
        }
    }

    // ---- victory ----

    @Test
    void noWinnerBelowTenPoints() {
        Game g = gameInPlay(2);
        Player p = g.getCurrentPlayer();
        giveCities(p, 3); // 6 points on top of the 2 from setup = 8
        assertEquals(8, p.getVictoryPoints());
        assertFalse(g.checkVictory());
        assertEquals(GamePhase.PLAYING, g.getPhase());
        assertNull(g.getWinner());
    }

    @Test
    void reachingTenPointsEndsTheGame() {
        Game g = gameInPlay(3);
        Player p = g.getCurrentPlayer();
        giveCities(p, 4); // 8 + 2 from setup = 10
        assertEquals(10, p.getVictoryPoints());
        assertTrue(g.checkVictory());
        assertEquals(GamePhase.GAME_OVER, g.getPhase());
        assertSame(p, g.getWinner());
    }

    @Test
    void goingPastTenAlsoWins() {
        Game g = gameInPlay(2);
        Player p = g.getCurrentPlayer();
        giveCities(p, 6);
        assertTrue(g.checkVictory());
        assertSame(p, g.getWinner());
    }

    @Test
    void longestRoadPointsCountTowardVictory() {
        Game g = gameInPlay(2);
        Player p = g.getCurrentPlayer();
        giveCities(p, 3); // 8
        p.setHasLongestRoad(true); // 10
        assertTrue(g.checkVictory());
        assertSame(p, g.getWinner());
    }

    @Test
    void currentPlayerWinsAheadOfOthersAtTheSameTime() {
        Game g = gameInPlay(3);
        Player current = g.getCurrentPlayer();
        Player other = g.getPlayers().get(2);
        giveCities(other, 4);
        giveCities(current, 4);
        assertTrue(g.checkVictory());
        assertSame(current, g.getWinner());
    }

    @Test
    void anotherPlayerWhoReachesTenIsFoundToo() {
        Game g = gameInPlay(3);
        Player other = g.getPlayers().get(2);
        giveCities(other, 4);
        assertTrue(g.checkVictory());
        assertSame(other, g.getWinner());
    }

    @Test
    void victoryCheckDoesNothingDuringSetup() {
        Game g = game(2);
        giveCities(g.getCurrentPlayer(), 5);
        assertFalse(g.checkVictory());
        assertEquals(GamePhase.SETUP, g.getPhase());
        assertNull(g.getWinner());
    }

    @Test
    void nothingCanHappenOnceTheGameIsOver() {
        Game g = gameInPlay(2);
        giveCities(g.getCurrentPlayer(), 4);
        g.checkVictory();
        assertFalse(g.canRoll());
        assertThrows(IllegalStateException.class, g::markRolled);
        assertThrows(IllegalStateException.class, g::endTurn);
        assertThrows(IllegalStateException.class, g::advanceSetup);
        assertTrue(g.checkVictory(), "stays over");
        assertEquals(GamePhase.GAME_OVER, g.getPhase());
    }

    @Test
    void winnerStaysTheSameOnceDecided() {
        Game g = gameInPlay(2);
        Player first = g.getCurrentPlayer();
        giveCities(first, 4);
        g.checkVictory();
        giveCities(g.getPlayers().get(1), 6);
        assertTrue(g.checkVictory());
        assertSame(first, g.getWinner());
    }

    // ---- longest road ----

    private void roads(Player p, Tile tile, int count) {
        for (int i = 0; i < count; i++) {
            Edge e = tile.getEdges().get(i);
            board.placeRoad(e, new Road(p, e));
        }
    }

    @Test
    void longestRoadStartsWithNobody() {
        Game g = game(2);
        assertNull(g.updateLongestRoadHolder());
        assertNull(g.getLongestRoadHolder());
    }

    @Test
    void fiveRoadsEarnTheBonusAndTwoPoints() {
        Game g = game(2);
        Player a = g.getPlayers().get(0);
        roads(a, board.getTile(0), 4);
        assertNull(g.updateLongestRoadHolder());
        assertFalse(a.hasLongestRoad());

        Edge fifth = board.getTile(0).getEdges().get(4);
        board.placeRoad(fifth, new Road(a, fifth));
        assertSame(a, g.updateLongestRoadHolder());
        assertSame(a, g.getLongestRoadHolder());
        assertTrue(a.hasLongestRoad());
        assertEquals(2, a.getVictoryPoints());
    }

    @Test
    void bonusMovesToALongerRoadAndComesOffTheOldHolder() {
        Game g = game(2);
        Player a = g.getPlayers().get(0);
        Player b = g.getPlayers().get(1);
        roads(a, board.getTile(0), 5);
        g.updateLongestRoadHolder();
        assertSame(a, g.getLongestRoadHolder());

        roads(b, board.getTile(18), 6);
        assertSame(b, g.updateLongestRoadHolder());
        assertFalse(a.hasLongestRoad());
        assertTrue(b.hasLongestRoad());
        assertEquals(0, a.getVictoryPoints());
        assertEquals(2, b.getVictoryPoints());
    }

    @Test
    void tieKeepsTheHolder() {
        Game g = game(2);
        Player a = g.getPlayers().get(0);
        Player b = g.getPlayers().get(1);
        roads(a, board.getTile(0), 5);
        g.updateLongestRoadHolder();
        roads(b, board.getTile(18), 5);
        assertSame(a, g.updateLongestRoadHolder());
        assertTrue(a.hasLongestRoad());
        assertFalse(b.hasLongestRoad());
    }

    @Test
    void anOpponentsSettlementCanTakeTheBonusAway() {
        Game g = game(2);
        Player a = g.getPlayers().get(0);
        Player b = g.getPlayers().get(1);
        roads(a, board.getTile(0), 5);
        g.updateLongestRoadHolder();
        assertTrue(a.hasLongestRoad());

        Vertex middle = board.getTile(0).getVertices().get(2); // between sides 1 and 2
        board.placeBuilding(middle, new Settlement(b, middle));
        assertNull(g.updateLongestRoadHolder());
        assertNull(g.getLongestRoadHolder());
        assertFalse(a.hasLongestRoad());
        assertEquals(0, a.getVictoryPoints());
    }

    @Test
    void updatingAgainChangesNothing() {
        Game g = game(2);
        Player a = g.getPlayers().get(0);
        roads(a, board.getTile(0), 5);
        g.updateLongestRoadHolder();
        g.updateLongestRoadHolder();
        assertTrue(a.hasLongestRoad());
        assertEquals(2, a.getVictoryPoints());
    }

    @Test
    void longestRoadCanDecideTheGame() {
        Game g = gameInPlay(2);
        Player p = g.getCurrentPlayer();
        giveCities(p, 3); // 6 + 2 from setup = 8
        assertFalse(g.checkVictory());
        roads(p, board.getTile(18), 6);
        g.updateLongestRoadHolder();
        assertTrue(g.checkVictory());
        assertSame(p, g.getWinner());
    }
}
