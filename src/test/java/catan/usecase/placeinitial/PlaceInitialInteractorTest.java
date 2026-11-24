package catan.usecase.placeinitial;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.SetupStep;
import catan.entity.Tile;
import catan.entity.TerrainType;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

class PlaceInitialInteractorTest {

    // ---- fakes ----

    private static class FakeGameRepository implements GameRepository {
        Game game;

        @Override
        public void save(Game game) {
            this.game = game;
        }

        @Override
        public Game get() {
            return game;
        }

        @Override
        public void clear() {
            game = null;
        }
    }

    private static class RecordingPresenter implements PlaceInitialOutputBoundary {
        final List<PlaceInitialOutputData> placements = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentPlacement(PlaceInitialOutputData outputData) {
            placements.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }

        PlaceInitialOutputData last() {
            return placements.get(placements.size() - 1);
        }
    }

    private final FakeGameRepository repository = new FakeGameRepository();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final PlaceInitialInteractor interactor = new PlaceInitialInteractor(repository, presenter);

    // ---- helpers ----

    private Game newGame(int players, WeatherData weather) {
        List<Player> list = new ArrayList<>();
        for (int i = 1; i <= players; i++) {
            list.add(new Player("P" + i));
        }
        Game game = new Game(BoardFactory.createStandardBoard(new Random(3)), list, weather,
                new Dice(new Random(1)));
        game.startGame();
        repository.save(game);
        return game;
    }

    private Game newGame(int players) {
        return newGame(players, WeatherData.none());
    }

    private void place(int id) {
        interactor.placeInitial(new PlaceInitialInputData(id));
    }

    /** The first vertex that is valid for the current player and meets the extra condition. */
    private static Vertex findSpot(Game game, Predicate<Vertex> extra) {
        Board board = game.getBoard();
        for (Vertex v : board.getVertices()) {
            if (board.isValidSettlementLocation(v, game.getCurrentPlayer(), true) && extra.test(v)) {
                return v;
            }
        }
        fail("no valid vertex found");
        return null;
    }

    private static Edge findRoadSpot(Game game) {
        for (Edge e : game.getLastPlacedSettlement().getAdjacentEdges()) {
            if (!e.isOccupied()) {
                return e;
            }
        }
        fail("no free edge next to the settlement");
        return null;
    }

    /** Settlement then road for the current player, at the first valid places. */
    private void takeTurn(Game game) {
        place(findSpot(game, v -> true).getId());
        place(findRoadSpot(game).getId());
    }

    private static int resourceTiles(Board board, Vertex v) {
        int count = 0;
        for (Tile t : board.getTilesAdjacentTo(v)) {
            if (t.getResourceType() != null) {
                count++;
            }
        }
        return count;
    }

    private static String snapshot(Game game) {
        Board board = game.getBoard();
        long vertices = board.getVertices().stream().filter(Vertex::isOccupied).count();
        long edges = board.getEdges().stream().filter(Edge::isOccupied).count();
        return game.getPhase() + "/" + game.getSetupStep() + "/" + game.getSetupIndex() + "/"
                + game.getCurrentPlayerIndex() + "/" + vertices + "/" + edges + "/"
                + game.getLastPlacedSettlement();
    }

    private void assertRejected(Game game, String message, int id) {
        String before = snapshot(game);
        int placements = presenter.placements.size();
        int errors = presenter.errors.size();
        place(id);
        assertEquals(errors + 1, presenter.errors.size());
        assertEquals(message, presenter.errors.get(presenter.errors.size() - 1));
        assertEquals(placements, presenter.placements.size());
        assertEquals(before, snapshot(game), "nothing should change");
    }

    // ---- no game / wrong phase ----

    @Test
    void noGameInProgress() {
        place(0);
        assertEquals(List.of("No game is in progress."), presenter.errors);
        assertTrue(presenter.placements.isEmpty());
    }

    @Test
    void afterSetupIsOverPlacementIsRefused() {
        Game game = newGame(2);
        while (game.getPhase() == GamePhase.SETUP) {
            takeTurn(game);
        }
        assertRejected(game, "Initial placement is over.", 0);
    }

    @Test
    void afterTheGameIsOverPlacementIsRefused() {
        Game game = newGame(2);
        while (game.getPhase() == GamePhase.SETUP) {
            takeTurn(game);
        }
        Player p = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
        assertTrue(game.checkVictory());
        assertRejected(game, "The game is over.", 0);
    }

    // ---- settlements ----

    @Test
    void placesTheFirstSettlementForPlayerOne() {
        Game game = newGame(2);
        Vertex spot = findSpot(game, v -> true);
        place(spot.getId());

        assertTrue(presenter.errors.isEmpty());
        assertTrue(spot.isOccupied());
        assertEquals("P1", spot.getBuilding().getOwner().getName());
        assertEquals(1, game.getPlayers().get(0).getBuildings().size());
        assertSame(spot, game.getLastPlacedSettlement());
        assertEquals(SetupStep.ROAD, game.getSetupStep());

        PlaceInitialOutputData out = presenter.last();
        assertEquals(SetupStep.SETTLEMENT, out.placed());
        assertEquals("P1", out.playerName());
        assertEquals(spot.getId(), out.locationId());
        assertFalse(out.setupComplete());
        assertEquals("P1", out.nextPlayerName(), "the same player now places the road");
        assertEquals(SetupStep.ROAD, out.nextStep());
        assertTrue(out.resourcesGained().isEmpty());
    }

    @Test
    void theFirstSettlementGivesNoResources() {
        Game game = newGame(2);
        place(findSpot(game, v -> resourceTiles(game.getBoard(), v) == 3).getId());
        assertEquals(0, game.getPlayers().get(0).getResources().getTotalResources());
        assertTrue(presenter.last().resourcesGained().isEmpty());
    }

    @Test
    void anUnknownVertexIsRefused() {
        Game game = newGame(2);
        assertRejected(game, "That is not a place on the board.", 999);
        assertRejected(game, "That is not a place on the board.", -1);
    }

    @Test
    void anOccupiedVertexIsRefused() {
        Game game = newGame(2);
        takeTurn(game); // P1 done, P2 to place
        int taken = game.getBoard().getVertices().stream().filter(Vertex::isOccupied)
                .findFirst().orElseThrow().getId();
        assertRejected(game, "That spot is already taken.", taken);
    }

    @Test
    void aVertexNextToAnotherSettlementIsRefused() {
        Game game = newGame(2);
        takeTurn(game);
        Vertex taken = game.getBoard().getVertices().stream().filter(Vertex::isOccupied)
                .findFirst().orElseThrow();
        Vertex neighbour = taken.getAdjacentVertices().get(0);
        assertRejected(game,
                "A settlement must be at least 2 edges away from any other settlement or city.",
                neighbour.getId());
    }

    @Test
    void aVertexTwoEdgesAwayIsAccepted() {
        Game game = newGame(2);
        takeTurn(game);
        Vertex taken = game.getBoard().getVertices().stream().filter(Vertex::isOccupied)
                .findFirst().orElseThrow();
        Vertex twoAway = taken.getAdjacentVertices().get(0).getAdjacentVertices().stream()
                .filter(v -> v != taken).findFirst().orElseThrow();
        place(twoAway.getId());
        assertTrue(presenter.errors.isEmpty());
        assertEquals("P2", twoAway.getBuilding().getOwner().getName());
    }

    // ---- roads ----

    @Test
    void placesTheRoadNextToTheSettlementAndPassesTheTurn() {
        Game game = newGame(3);
        Vertex spot = findSpot(game, v -> true);
        place(spot.getId());
        Edge edge = findRoadSpot(game);
        place(edge.getId());

        assertTrue(presenter.errors.isEmpty());
        assertTrue(edge.isOccupied());
        assertEquals("P1", edge.getRoad().getOwner().getName());
        assertEquals(1, game.getPlayers().get(0).getRoads().size());
        assertNull(game.getLastPlacedSettlement());
        assertEquals(SetupStep.SETTLEMENT, game.getSetupStep());
        assertEquals(1, game.getSetupIndex());

        PlaceInitialOutputData out = presenter.last();
        assertEquals(SetupStep.ROAD, out.placed());
        assertEquals("P1", out.playerName());
        assertEquals(edge.getId(), out.locationId());
        assertFalse(out.setupComplete());
        assertEquals("P2", out.nextPlayerName());
        assertEquals(SetupStep.SETTLEMENT, out.nextStep());
        assertTrue(out.resourcesGained().isEmpty());
    }

    @Test
    void aRoadAwayFromTheSettlementIsRefused() {
        Game game = newGame(2);
        Vertex spot = findSpot(game, v -> true);
        place(spot.getId());
        Edge far = game.getBoard().getEdges().stream()
                .filter(e -> e.getVertex1() != spot && e.getVertex2() != spot).findFirst().orElseThrow();
        assertRejected(game, "The road must touch the settlement you just placed.", far.getId());

        place(findRoadSpot(game).getId()); // the right one still works afterwards
        assertEquals(1, presenter.errors.size(), "only the refused road was an error");
        assertEquals(1, game.getSetupIndex());
    }

    @Test
    void aRoadOnAnOccupiedEdgeIsRefused() {
        Game game = newGame(2);
        takeTurn(game); // P1: settlement at s, road s-n
        Vertex s = game.getBoard().getVertices().stream().filter(Vertex::isOccupied)
                .findFirst().orElseThrow();
        Vertex n = s.getAdjacentVertices().get(0);
        Vertex m = n.getAdjacentVertices().stream().filter(v -> v != s).findFirst().orElseThrow();
        Edge nm = n.getAdjacentEdges().stream().filter(e -> e.getOtherVertex(n) == m).findFirst().orElseThrow();
        // make sure the P1 road we want blocked really sits on n-m, and that P2 can settle at m
        Edge sn = s.getAdjacentEdges().stream().filter(e -> e.getOtherVertex(s) == n).findFirst().orElseThrow();
        if (!sn.isOccupied()) {
            game.getBoard().placeRoad(sn, new Road(game.getPlayers().get(0), sn));
        }
        game.getBoard().placeRoad(nm, new Road(game.getPlayers().get(0), nm));

        place(m.getId()); // P2 settles at m
        assertTrue(presenter.errors.isEmpty(), "P2 settles two edges from P1: " + presenter.errors);
        assertRejected(game, "That edge already has a road.", nm.getId());
    }

    @Test
    void anUnknownEdgeIsRefused() {
        Game game = newGame(2);
        place(findSpot(game, v -> true).getId());
        assertRejected(game, "That is not a place on the board.", 500);
    }

    // ---- full setup ----

    @Test
    void fullSetupFollowsTheSnakeOrderForTwoThreeAndFourPlayers() {
        for (int n = 2; n <= 4; n++) {
            presenter.placements.clear();
            Game game = newGame(n);
            List<Player> order = game.getSetupOrder();
            while (game.getPhase() == GamePhase.SETUP) {
                takeTurn(game);
            }

            assertTrue(presenter.errors.isEmpty(), n + " players: " + presenter.errors);
            assertEquals(4 * n, presenter.placements.size(), n + " players");
            for (int turn = 0; turn < 2 * n; turn++) {
                PlaceInitialOutputData settlement = presenter.placements.get(2 * turn);
                PlaceInitialOutputData road = presenter.placements.get(2 * turn + 1);
                assertEquals(SetupStep.SETTLEMENT, settlement.placed());
                assertEquals(SetupStep.ROAD, road.placed());
                assertEquals(order.get(turn).getName(), settlement.playerName(), n + " players, turn " + turn);
                assertEquals(order.get(turn).getName(), road.playerName());
            }

            assertEquals(GamePhase.PLAYING, game.getPhase());
            assertEquals("P1", game.getCurrentPlayer().getName());
            assertTrue(game.canRoll());
            for (Player p : game.getPlayers()) {
                assertEquals(2, p.getBuildings().size(), p.getName());
                assertEquals(2, p.getRoads().size(), p.getName());
                assertEquals(2, p.getVictoryPoints(), p.getName());
            }
        }
    }

    @Test
    void onlyTheLastRoadCompletesSetup() {
        Game game = newGame(3);
        while (game.getPhase() == GamePhase.SETUP) {
            takeTurn(game);
        }
        for (int i = 0; i < presenter.placements.size() - 1; i++) {
            assertFalse(presenter.placements.get(i).setupComplete(), "placement " + i);
        }
        PlaceInitialOutputData last = presenter.last();
        assertEquals(SetupStep.ROAD, last.placed());
        assertTrue(last.setupComplete());
        assertEquals("P1", last.nextPlayerName());
        assertNull(last.nextStep());
    }

    @Test
    void everyRoadStartsFromThatPlayersOwnNewSettlement() {
        Game game = newGame(4);
        while (game.getPhase() == GamePhase.SETUP) {
            takeTurn(game);
        }
        for (Player p : game.getPlayers()) {
            for (Road road : p.getRoads()) {
                Edge e = road.getLocation();
                boolean touchesOwn = false;
                for (Vertex v : List.of(e.getVertex1(), e.getVertex2())) {
                    if (v.isOccupied() && v.getBuilding().getOwner() == p) {
                        touchesOwn = true;
                    }
                }
                assertTrue(touchesOwn, p.getName() + " has a road away from their settlements");
            }
        }
    }

    // ---- starting resources ----

    @Test
    void theSecondSettlementGivesOneResourcePerAdjacentResourceTile() {
        Game game = newGame(2);
        takeTurn(game); // P1 first
        takeTurn(game); // P2 first
        Player p2 = game.getCurrentPlayer();
        assertEquals("P2", p2.getName());
        assertEquals(0, p2.getResources().getTotalResources());

        Vertex spot = findSpot(game, v -> game.getBoard().getTilesAdjacentTo(v).size() == 3
                && resourceTiles(game.getBoard(), v) == 3);
        Map<ResourceType, Integer> expected = new EnumMap<>(ResourceType.class);
        for (Tile t : game.getBoard().getTilesAdjacentTo(spot)) {
            expected.merge(t.getResourceType(), 1, Integer::sum);
        }
        place(spot.getId());

        assertEquals(expected, presenter.last().resourcesGained());
        assertEquals(3, p2.getResources().getTotalResources());
        for (ResourceType type : ResourceType.values()) {
            assertEquals(expected.getOrDefault(type, 0), p2.getResources().getResourceCount(type), type.name());
        }
        assertEquals(0, game.getPlayers().get(0).getResources().getTotalResources(), "P1 gets nothing yet");
    }

    @Test
    void theDesertGivesNothing() {
        Game game = newGame(2);
        takeTurn(game);
        takeTurn(game);
        Vertex spot = findSpot(game, v -> game.getBoard().getTilesAdjacentTo(v).stream()
                .anyMatch(t -> t.getTerrain() == TerrainType.DESERT));
        int expected = resourceTiles(game.getBoard(), spot);
        assertTrue(expected < game.getBoard().getTilesAdjacentTo(spot).size());
        place(spot.getId());
        assertEquals(expected, game.getCurrentPlayer().getResources().getTotalResources());
        assertEquals(expected, presenter.last().resourcesGained().values().stream()
                .mapToInt(Integer::intValue).sum());
    }

    @Test
    void aSettlementOnTheCoastGetsOnlyItsOneOrTwoTiles() {
        Game game = newGame(2);
        takeTurn(game);
        takeTurn(game);
        Vertex spot = findSpot(game, v -> game.getBoard().getTilesAdjacentTo(v).size() == 1
                && resourceTiles(game.getBoard(), v) == 1);
        place(spot.getId());
        assertEquals(1, game.getCurrentPlayer().getResources().getTotalResources());
    }

    @Test
    void weatherDoesNotChangeStartingResources() {
        Game game = newGame(2, WeatherData.fromWmoCode(0)); // sunny: lumber x2
        takeTurn(game);
        takeTurn(game);
        Vertex spot = findSpot(game, v -> game.getBoard().getTilesAdjacentTo(v).size() == 3
                && resourceTiles(game.getBoard(), v) == 3);
        place(spot.getId());
        assertEquals(3, game.getCurrentPlayer().getResources().getTotalResources());
    }

    @Test
    void everyPlayersSecondSettlementPaysAndTheFirstDoesNot() {
        for (int n = 2; n <= 4; n++) {
            presenter.placements.clear();
            Game game = newGame(n);
            Map<String, Integer> expected = new java.util.HashMap<>();
            while (game.getPhase() == GamePhase.SETUP) {
                boolean second = game.getSetupIndex() >= n;
                Vertex spot = findSpot(game, v -> true);
                if (second) {
                    expected.merge(game.getCurrentPlayer().getName(),
                            resourceTiles(game.getBoard(), spot), Integer::sum);
                }
                place(spot.getId());
                place(findRoadSpot(game).getId());
            }
            for (Player p : game.getPlayers()) {
                assertEquals(expected.getOrDefault(p.getName(), 0), p.getResources().getTotalResources(),
                        n + " players, " + p.getName());
            }
            long paying = presenter.placements.stream()
                    .filter(o -> o.placed() == SetupStep.SETTLEMENT && !o.resourcesGained().isEmpty()).count();
            assertTrue(paying <= n, "at most one paying placement per player: " + paying);
        }
    }

    @Test
    void refusedPlacementsNeverGiveResources() {
        Game game = newGame(2);
        takeTurn(game);
        takeTurn(game);
        int taken = game.getBoard().getVertices().stream().filter(Vertex::isOccupied)
                .findFirst().orElseThrow().getId();
        assertRejected(game, "That spot is already taken.", taken);
        assertEquals(0, game.getCurrentPlayer().getResources().getTotalResources());
    }

    // ---- construction ----

    @Test
    void constructorAndInputRejectNulls() {
        assertThrows(NullPointerException.class, () -> new PlaceInitialInteractor(null, presenter));
        assertThrows(NullPointerException.class, () -> new PlaceInitialInteractor(repository, null));
        assertThrows(NullPointerException.class, () -> interactor.placeInitial(null));
    }
}
