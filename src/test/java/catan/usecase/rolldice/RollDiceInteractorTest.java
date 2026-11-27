package catan.usecase.rolldice;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.Settlement;
import catan.entity.Tile;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.usecase.FakeGameRepository;
import catan.usecase.TestGames;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * The resource amounts themselves (settlement x1, city x2, weather, 7, desert) are tested in
 * ProductionCalculatorTest. These tests check that the interactor hands the result to the
 * players, keeps the game state right, and refuses rolls that are not allowed.
 */
class RollDiceInteractorTest {

    // ---- fakes ----

    /** A Random whose dice come out as chosen: the Dice asks it for nextInt(6) + 1 twice per roll. */
    private static final class FixedRandom extends Random {
        private final List<Integer> faces = new ArrayList<>();

        /** The next roll will total this much. */
        void queueTotal(int total) {
            int first = Math.min(6, total - 1);
            faces.add(first);
            faces.add(total - first);
        }

        @Override
        public int nextInt(int bound) {
            assertEquals(6, bound, "only a six-sided die is expected");
            return faces.remove(0) - 1;
        }
    }

    private static final class RecordingPresenter implements RollDiceOutputBoundary {
        final List<RollDiceOutputData> rolled = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentRolled(RollDiceOutputData outputData) {
            rolled.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }
    }

    private final FakeGameRepository repository = new FakeGameRepository();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final RollDiceInteractor interactor = new RollDiceInteractor(repository, presenter);
    private final FixedRandom random = new FixedRandom();

    // ---- helpers ----

    /** A game in regular play (setup finished), Ann to roll, with dice that follow {@link #random}. */
    private Game playingGame(WeatherData weather) {
        List<Player> players = List.of(new Player("Ann"), new Player("Bob"));
        Game game = new Game(BoardFactory.createStandardBoard(new Random(4)), players, weather, new Dice(random));
        game.startGame();
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex v = game.getBoard().getVertices().stream()
                    .filter(x -> game.getBoard().isValidSettlementLocation(x, p, true))
                    .findFirst().orElseThrow();
            game.getBoard().placeBuilding(v, new Settlement(p, v));
            game.setLastPlacedSettlement(v);
            game.advanceSetup();
            Edge e = game.getBoard().getEdges().stream()
                    .filter(x -> game.getBoard().isValidRoadLocation(x, p, game.getLastPlacedSettlement()))
                    .findFirst().orElseThrow();
            game.getBoard().placeRoad(e, new Road(p, e));
            game.advanceSetup();
        }
        repository.game = game;
        return game;
    }

    /** A tile with a number token and no building on any of its corners, so only what the test adds counts. */
    private static Tile quietTile(Game game, ResourceType resource) {
        return game.getBoard().getTiles().stream()
                .filter(Tile::hasNumberToken)
                .filter(t -> resource == null || t.getResourceType() == resource)
                .filter(t -> t.getVertices().stream().noneMatch(Vertex::isOccupied))
                .findFirst().orElseThrow();
    }

    private static void settle(Player p, Vertex v) {
        v.setBuilding(new Settlement(p, v));
        p.addBuilding(v.getBuilding());
    }

    private static void upgrade(Player p, Vertex v) {
        p.removeBuilding(v.getBuilding());
        v.setBuilding(new City(p, v));
        p.addBuilding(v.getBuilding());
    }

    private void roll() {
        interactor.rollDice(new RollDiceInputData());
    }

    private static int count(Player p, ResourceType type) {
        return p.getResources().getResourceCount(type);
    }

    private static String snapshot(Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append(game.getPhase()).append('/').append(game.getCurrentPlayerIndex()).append('/')
                .append(game.hasRolled()).append('/').append(game.getDice().getTotal());
        for (Player p : game.getPlayers()) {
            sb.append('/').append(p.getVictoryPoints()).append(':').append(p.getResources().getTotalResources());
        }
        return sb.toString();
    }

    private void assertRejected(Game game, String message) {
        String before = snapshot(game);
        int saves = repository.gameSaves;
        roll();
        assertEquals(List.of(message), presenter.errors);
        assertTrue(presenter.rolled.isEmpty());
        assertEquals(before, snapshot(game), "nothing should change");
        assertEquals(saves, repository.gameSaves);
    }

    // ---- the result reaches the players' hands ----

    @Test
    void aSettlementOnTheRolledTileAddsOneResourceToTheHand() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, null);
        settle(ann, tile.getVertices().get(0));
        random.queueTotal(tile.getNumberToken());

        roll();

        assertTrue(presenter.errors.isEmpty());
        assertEquals(1, count(ann, tile.getResourceType()));
        assertEquals(0, count(game.getPlayers().get(1), tile.getResourceType()));
    }

    @Test
    void aCityOnTheRolledTileAddsTwoResourcesToTheHand() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, null);
        settle(ann, tile.getVertices().get(0));
        upgrade(ann, tile.getVertices().get(0));
        random.queueTotal(tile.getNumberToken());

        roll();

        assertEquals(2, count(ann, tile.getResourceType()));
    }

    @Test
    void theGamesWeatherBonusDoublesWhatIsAddedToTheHand() {
        Game game = playingGame(WeatherData.fromWmoCode(0)); // sunny: lumber x2
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, ResourceType.LUMBER);
        settle(ann, tile.getVertices().get(0));
        random.queueTotal(tile.getNumberToken());

        roll();

        assertEquals(2, count(ann, ResourceType.LUMBER));
    }

    @Test
    void aSevenGivesNothingButTheRollStillCounts() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        settle(ann, quietTile(game, null).getVertices().get(0));
        int before = ann.getResources().getTotalResources();
        random.queueTotal(7);

        roll();

        assertTrue(presenter.errors.isEmpty());
        assertEquals(before, ann.getResources().getTotalResources());
        assertTrue(game.hasRolled());
        assertTrue(presenter.rolled.get(0).gains().isEmpty());
    }

    @Test
    void whenNobodyIsNextToTheRolledTileNothingIsGivenAndItIsNotAnError() {
        Game game = playingGame(WeatherData.none());
        Tile tile = quietTile(game, null);
        random.queueTotal(tile.getNumberToken());

        roll();

        assertTrue(presenter.errors.isEmpty());
        assertTrue(presenter.rolled.get(0).gains().isEmpty());
        assertTrue(game.hasRolled());
    }

    // ---- state ----

    @Test
    void aRollMarksTheTurnAsRolledAndSavesTheGame() {
        Game game = playingGame(WeatherData.none());
        random.queueTotal(7);
        assertFalse(game.hasRolled());

        roll();

        assertTrue(game.hasRolled());
        assertEquals(1, repository.gameSaves);
        assertSame(game, repository.game);
    }

    @Test
    void rollingDoesNotChangePointsOrBuildings() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, null);
        settle(ann, tile.getVertices().get(0));
        int points = ann.getVictoryPoints();
        int buildings = ann.getBuildings().size();
        random.queueTotal(tile.getNumberToken());

        roll();

        assertEquals(points, ann.getVictoryPoints());
        assertEquals(buildings, ann.getBuildings().size());
        assertEquals(GamePhase.PLAYING, game.getPhase());
    }

    @Test
    void thePlayerCanRollAgainOnTheNextTurn() {
        Game game = playingGame(WeatherData.none());
        random.queueTotal(7);
        roll();
        game.endTurn();
        random.queueTotal(7);

        roll();

        assertTrue(presenter.errors.isEmpty());
        assertEquals(2, presenter.rolled.size());
        assertEquals("Bob", presenter.rolled.get(1).playerName());
    }

    // ---- what is reported ----

    @Test
    void theOutputHasTheDiceTheTotalThePlayerAndTheGains() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, null);
        settle(ann, tile.getVertices().get(0));
        int total = tile.getNumberToken();
        random.queueTotal(total);

        roll();

        RollDiceOutputData out = presenter.rolled.get(0);
        assertEquals("Ann", out.playerName());
        assertEquals(total, out.total());
        assertEquals(out.dieOne() + out.dieTwo(), out.total());
        assertEquals(game.getDice().getDie1(), out.dieOne());
        assertEquals(game.getDice().getDie2(), out.dieTwo());
        assertEquals(Map.of("Ann", Map.of(tile.getResourceType().name(), 1)), out.gains());
    }

    @Test
    void playersWhoReceiveNothingAreNotInTheGains() {
        Game game = playingGame(WeatherData.none());
        Player ann = game.getPlayers().get(0);
        Tile tile = quietTile(game, null);
        settle(ann, tile.getVertices().get(0));
        random.queueTotal(tile.getNumberToken());

        roll();

        assertEquals(java.util.Set.of("Ann"), presenter.rolled.get(0).gains().keySet());
    }

    // ---- refusals ----

    @Test
    void noGameInProgress() {
        roll();
        assertEquals(List.of("No game is in progress."), presenter.errors);
        assertTrue(presenter.rolled.isEmpty());
    }

    @Test
    void cannotRollDuringSetup() {
        Game game = TestGames.setupGame(4, "Ann", "Bob");
        repository.game = game;
        assertRejected(game, "Finish placing the starting settlements and roads first.");
    }

    @Test
    void cannotRollOnceTheGameIsOver() {
        Game game = playingGame(WeatherData.none());
        Player p = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
        assertTrue(game.checkVictory());
        assertRejected(game, "The game is over.");
    }

    @Test
    void cannotRollTwiceInOneTurn() {
        Game game = playingGame(WeatherData.none());
        random.queueTotal(7);
        roll();
        presenter.rolled.clear();
        repository.gameSaves = 0;

        assertRejected(game, "You have already rolled this turn.");
    }

    // ---- construction ----

    @Test
    void constructorAndInputRejectNulls() {
        assertThrows(NullPointerException.class, () -> new RollDiceInteractor(null, presenter));
        assertThrows(NullPointerException.class, () -> new RollDiceInteractor(repository, null));
        assertThrows(NullPointerException.class, () -> interactor.rollDice(null));
    }
}
