package catan.usecase.restartgame;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RestartGameInteractorTest {

    // ---- fakes ----

    private static class FakeGameRepository implements GameRepository {
        Game game;
        int clears;

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
            clears++;
        }
    }

    private static class RecordingPresenter implements RestartGameOutputBoundary {
        final List<RestartGameOutputData> menus = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentReturnedToMenu(RestartGameOutputData outputData) {
            menus.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }
    }

    private final FakeGameRepository repository = new FakeGameRepository();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final RestartGameInteractor interactor = new RestartGameInteractor(repository, presenter);

    private Game newGame() {
        Game game = new Game(BoardFactory.createStandardBoard(new Random(1)),
                List.of(new Player("Ann"), new Player("Bob")), WeatherData.none(), new Dice(new Random(1)));
        game.startGame();
        repository.save(game);
        return game;
    }

    /** Plays a game to the end by giving the player to move enough cities. */
    private Game finishedGame() {
        Game game = newGame();
        // skip setup by driving it through the entity API
        while (game.getPhase() == catan.entity.GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex spot = game.getBoard().getVertices().stream()
                    .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
                    .findFirst().orElseThrow();
            game.getBoard().placeBuilding(spot, new catan.entity.Settlement(p, spot));
            game.setLastPlacedSettlement(spot);
            game.advanceSetup();
            var road = spot.getAdjacentEdges().get(0);
            game.getBoard().placeRoad(road, new catan.entity.Road(p, road));
            game.advanceSetup();
        }
        Player p = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
        assertTrue(game.checkVictory());
        return game;
    }

    private void restart() {
        interactor.restartGame(new RestartGameInputData());
    }

    // ---- success ----

    @Test
    void clearsTheFinishedGameAndReturnsToTheMenu() {
        finishedGame();
        restart();

        assertTrue(presenter.errors.isEmpty());
        assertNull(repository.get());
        assertEquals(1, repository.clears);
        assertEquals(List.of(new RestartGameOutputData()), presenter.menus);
    }

    @Test
    void theResultIsStillAvailableBeforeLeaving() {
        Game game = finishedGame();
        GameResult result = GameResult.from(game);
        restart();
        assertEquals(game.getWinner().getName(), result.winnerName());
        assertEquals(2, result.scores().size());
    }

    // ---- refusals ----

    @Test
    void noGameToLeave() {
        restart();
        assertEquals(List.of("No game is in progress."), presenter.errors);
        assertTrue(presenter.menus.isEmpty());
        assertEquals(0, repository.clears);
    }

    @Test
    void aGameStillInProgressIsKept() {
        Game game = newGame();
        restart();
        restart();

        assertEquals(List.of("The game is not over yet.", "The game is not over yet."), presenter.errors);
        assertTrue(presenter.menus.isEmpty());
        assertSame(game, repository.get());
        assertEquals(0, repository.clears);
    }

    @Test
    void leavingTwiceOnlyWorksOnce() {
        finishedGame();
        restart();
        restart();
        assertEquals(1, presenter.menus.size());
        assertEquals(List.of("No game is in progress."), presenter.errors);
        assertEquals(1, repository.clears);
    }

    // ---- construction ----

    @Test
    void constructorAndInputRejectNulls() {
        assertThrows(NullPointerException.class, () -> new RestartGameInteractor(null, presenter));
        assertThrows(NullPointerException.class, () -> new RestartGameInteractor(repository, null));
        assertThrows(NullPointerException.class, () -> interactor.restartGame(null));
    }
}
