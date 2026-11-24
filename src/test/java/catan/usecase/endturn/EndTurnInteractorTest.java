package catan.usecase.endturn;

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
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class EndTurnInteractorTest {

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

    private static class RecordingPresenter implements EndTurnOutputBoundary {
        final List<EndTurnOutputData> ended = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentTurnEnded(EndTurnOutputData outputData) {
            ended.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }
    }

    private final FakeGameRepository repository = new FakeGameRepository();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final EndTurnInteractor interactor = new EndTurnInteractor(repository, presenter);

    // ---- helpers ----

    /** A game that is still in setup. */
    private Game setupGame(int players) {
        List<Player> list = new ArrayList<>();
        for (int i = 1; i <= players; i++) {
            list.add(new Player("P" + i));
        }
        Game game = new Game(BoardFactory.createStandardBoard(new Random(4)), list, WeatherData.none(),
                new Dice(new Random(1)));
        game.startGame();
        repository.save(game);
        return game;
    }

    /** A game in regular play, with Player 1 to roll. */
    private Game playingGame(int players) {
        Game game = setupGame(players);
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex spot = game.getBoard().getVertices().stream()
                    .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
                    .findFirst().orElseThrow();
            game.getBoard().placeBuilding(spot, new Settlement(p, spot));
            game.setLastPlacedSettlement(spot);
            game.advanceSetup();
            Edge road = spot.getAdjacentEdges().get(0);
            game.getBoard().placeRoad(road, new Road(p, road));
            game.advanceSetup();
        }
        return game;
    }

    private void endTurn() {
        interactor.endTurn(new EndTurnInputData());
    }

    private static String snapshot(Game game) {
        StringBuilder sb = new StringBuilder();
        sb.append(game.getPhase()).append('/').append(game.getCurrentPlayerIndex()).append('/')
                .append(game.hasRolled());
        for (Player p : game.getPlayers()) {
            sb.append('/').append(p.getVictoryPoints()).append(':').append(p.getResources().getTotalResources());
        }
        return sb.toString();
    }

    private void assertRejected(Game game, String message) {
        String before = snapshot(game);
        int errors = presenter.errors.size();
        int ended = presenter.ended.size();
        endTurn();
        assertEquals(errors + 1, presenter.errors.size());
        assertEquals(message, presenter.errors.get(presenter.errors.size() - 1));
        assertEquals(ended, presenter.ended.size());
        assertEquals(before, snapshot(game), "nothing should change");
    }

    // ---- success ----

    @Test
    void passesTheTurnToTheNextPlayerAfterRolling() {
        Game game = playingGame(3);
        game.markRolled();
        endTurn();

        assertTrue(presenter.errors.isEmpty());
        assertEquals(1, presenter.ended.size());
        EndTurnOutputData out = presenter.ended.get(0);
        assertEquals("P1", out.previousPlayerName());
        assertEquals("P2", out.nextPlayerName());
        assertEquals("P2", game.getCurrentPlayer().getName());
    }

    @Test
    void theNextPlayerStartsWithAFreshRoll() {
        Game game = playingGame(2);
        game.markRolled();
        endTurn();
        assertFalse(game.hasRolled());
        assertTrue(game.canRoll());
    }

    @Test
    void turnsCycleThroughEveryPlayerAndBackToPlayerOne() {
        for (int n = 2; n <= 4; n++) {
            presenter.ended.clear();
            Game game = playingGame(n);
            for (int turn = 0; turn < 2 * n + 1; turn++) {
                game.markRolled();
                endTurn();
            }
            assertTrue(presenter.errors.isEmpty(), n + " players");
            assertEquals(2 * n + 1, presenter.ended.size());
            for (int turn = 0; turn < presenter.ended.size(); turn++) {
                EndTurnOutputData out = presenter.ended.get(turn);
                assertEquals("P" + (turn % n + 1), out.previousPlayerName(), n + " players, turn " + turn);
                assertEquals("P" + ((turn + 1) % n + 1), out.nextPlayerName(), n + " players, turn " + turn);
            }
            assertEquals("P2", game.getCurrentPlayer().getName(), "after 2n + 1 turns it is P2's turn");
        }
    }

    @Test
    void theLastPlayerPassesBackToPlayerOne() {
        Game game = playingGame(2);
        game.markRolled();
        endTurn();
        game.markRolled();
        endTurn();
        EndTurnOutputData out = presenter.ended.get(1);
        assertEquals("P2", out.previousPlayerName());
        assertEquals("P1", out.nextPlayerName());
    }

    @Test
    void endingATurnLeavesResourcesAndPointsAlone() {
        Game game = playingGame(2);
        Player p1 = game.getPlayers().get(0);
        p1.getResources().addResource(ResourceType.ORE, 3);
        p1.addBuilding(new City(p1, new Vertex(900)));
        int points = p1.getVictoryPoints();
        game.markRolled();
        endTurn();
        assertEquals(3, p1.getResources().getResourceCount(ResourceType.ORE));
        assertEquals(points, p1.getVictoryPoints());
        assertEquals(GamePhase.PLAYING, game.getPhase());
    }

    // ---- refusals ----

    @Test
    void noGameInProgress() {
        endTurn();
        assertEquals(List.of("No game is in progress."), presenter.errors);
        assertTrue(presenter.ended.isEmpty());
    }

    @Test
    void cannotEndATurnDuringSetup() {
        Game game = setupGame(2);
        assertRejected(game, "Finish placing the starting settlements and roads first.");
    }

    @Test
    void cannotEndATurnBeforeRolling() {
        Game game = playingGame(2);
        assertRejected(game, "Roll the dice before ending your turn.");
        assertEquals("P1", game.getCurrentPlayer().getName());
    }

    @Test
    void cannotEndTwoTurnsInARowWithoutTheNextPlayerRolling() {
        Game game = playingGame(2);
        game.markRolled();
        endTurn();
        assertRejected(game, "Roll the dice before ending your turn.");
        assertEquals("P2", game.getCurrentPlayer().getName());
    }

    @Test
    void cannotEndATurnOnceTheGameIsOver() {
        Game game = playingGame(2);
        game.markRolled();
        Player p = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
        assertTrue(game.checkVictory());
        assertRejected(game, "The game is over.");
        assertSame(p, game.getWinner());
    }

    // ---- construction ----

    @Test
    void constructorAndInputRejectNulls() {
        assertThrows(NullPointerException.class, () -> new EndTurnInteractor(null, presenter));
        assertThrows(NullPointerException.class, () -> new EndTurnInteractor(repository, null));
        assertThrows(NullPointerException.class, () -> interactor.endTurn(null));
    }
}
