package catan.interfaceadapter;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.City;
import catan.entity.CityLocation;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.Vertex;
import catan.framework.persistence.InMemoryGameRepository;
import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.controller.PlaceInitialController;
import catan.interfaceadapter.controller.RestartGameController;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.presenter.EndTurnPresenter;
import catan.interfaceadapter.presenter.GameOverPresenter;
import catan.interfaceadapter.presenter.PlaceInitialPresenter;
import catan.interfaceadapter.presenter.RestartGamePresenter;
import catan.interfaceadapter.presenter.StartGamePresenter;
import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import catan.usecase.endturn.EndTurnInteractor;
import catan.usecase.placeinitial.PlaceInitialInteractor;
import catan.usecase.weather.WeatherGateway;
import catan.usecase.weather.WeatherUnavailableException;
import catan.usecase.restartgame.GameResult;
import catan.usecase.restartgame.RestartGameInteractor;
import catan.usecase.startgame.StartGameInteractor;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/**
 * Plays a whole game through the controllers, the real use cases and the presenters, and
 * checks what the screens would show. This is the wiring AppBuilder does for the real app.
 */
class GameFlowTest {
    private final InMemoryGameRepository repository = new InMemoryGameRepository();
    private boolean weatherDown;

    private final StartMenuViewModel startMenu =
            new StartMenuViewModel(CityLocation.defaults().stream().map(CityLocation::getName).toList());
    private final GameViewModel gameView = new GameViewModel();
    private final GameOverViewModel gameOverView = new GameOverViewModel();
    private final ViewManagerModel viewManager = new ViewManagerModel();

    private final WeatherGateway weather = city -> {
        if (weatherDown) {
            throw new WeatherUnavailableException("offline");
        }
        return 61; // rainy: grain x2
    };

    private final StartGameController startGame = new StartGameController(new StartGameInteractor(
            repository, weather, new StartGamePresenter(startMenu, gameView, viewManager), new Random(8)));
    private final PlaceInitialController placeInitial = new PlaceInitialController(
            new PlaceInitialInteractor(repository, new PlaceInitialPresenter(gameView)));
    private final EndTurnController endTurn = new EndTurnController(
            new EndTurnInteractor(repository, new EndTurnPresenter(gameView)));
    private final RestartGameController restart = new RestartGameController(new RestartGameInteractor(
            repository, new RestartGamePresenter(startMenu, gameView, gameOverView, viewManager)));
    private final GameOverPresenter gameOverPresenter = new GameOverPresenter(gameOverView, viewManager);

    private Game game() {
        return repository.get();
    }

    private void clickFirstValidSettlement() {
        Game g = game();
        Player p = g.getCurrentPlayer();
        Vertex spot = g.getBoard().getVertices().stream()
                .filter(v -> g.getBoard().isValidSettlementLocation(v, p, true)).findFirst().orElseThrow();
        placeInitial.place(spot.getId());
    }

    private void clickFirstFreeRoad() {
        Edge edge = game().getLastPlacedSettlement().getAdjacentEdges().stream()
                .filter(e -> !e.isOccupied()).findFirst().orElseThrow();
        placeInitial.place(edge.getId());
    }

    @Test
    void theWholeGameFromStartMenuToVictoryAndBack() {
        // --- start menu ---
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
        assertTrue(startMenu.getCityNames().contains("Toronto"));

        startGame.startGame(List.of("Ann", "Bob"), null);
        assertEquals("Please select a city.", startMenu.getErrorMessage());
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());

        startGame.startGame(List.of("Ann", "Bob"), "Toronto");
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertEquals("", startMenu.getErrorMessage());
        assertEquals("Weather in Toronto: Rainy", startMenu.getWeatherText());
        assertEquals("Bonus: Grain x2", startMenu.getBonusText());
        assertEquals(List.of("Ann", "Bob"), gameView.getPlayerNames());
        assertEquals("Ann", gameView.getCurrentPlayerName());
        assertEquals("Place a settlement", gameView.getInstruction());

        // --- setup: a bad click first, then the real placements ---
        placeInitial.place(999);
        assertEquals("That is not a place on the board.", gameView.getErrorMessage());
        assertEquals("Ann", gameView.getCurrentPlayerName());

        clickFirstValidSettlement();
        assertEquals("", gameView.getErrorMessage());
        assertEquals("Ann", gameView.getCurrentPlayerName());
        assertEquals("Place a road next to your new settlement", gameView.getInstruction());
        clickFirstFreeRoad();
        assertEquals("Bob", gameView.getCurrentPlayerName());
        assertEquals("Place a settlement", gameView.getInstruction());

        // Ann's first turn is placed above; the snake order leaves Bob, Bob, then Ann
        while (game().getPhase() == GamePhase.SETUP) {
            clickFirstValidSettlement();
            clickFirstFreeRoad();
        }
        assertEquals("Ann", gameView.getCurrentPlayerName());
        assertEquals("Roll the dice", gameView.getInstruction());
        assertEquals("Setup complete.", gameView.getMessage());
        for (Player p : game().getPlayers()) {
            assertEquals(2, p.getBuildings().size());
            assertEquals(2, p.getRoads().size());
        }

        // --- turns ---
        endTurn.endTurn();
        assertEquals("Roll the dice before ending your turn.", gameView.getErrorMessage());
        assertEquals("Ann", gameView.getCurrentPlayerName());

        game().markRolled(); // the roll-dice use case does this
        endTurn.endTurn();
        assertEquals("", gameView.getErrorMessage());
        assertEquals("Bob", gameView.getCurrentPlayerName());
        assertEquals("Roll the dice", gameView.getInstruction());
        assertEquals("Ann ended their turn.", gameView.getMessage());

        // --- victory ---
        Player bob = game().getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            bob.addBuilding(new City(bob, new Vertex(2000 + i)));
        }
        assertTrue(game().checkVictory());
        gameOverPresenter.presentGameOver(GameResult.from(game()));
        assertEquals(Screen.GAME_OVER, viewManager.getActiveScreen());
        assertEquals("Bob", gameOverView.getWinnerName());
        assertEquals(List.of(new ScoreEntry("Ann", 2, false), new ScoreEntry("Bob", 10, true)),
                gameOverView.getScores());

        // nothing more can be played
        endTurn.endTurn();
        assertEquals("The game is over.", gameView.getErrorMessage());

        // --- back to the menu, and a second game works ---
        restart.returnToMenu();
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
        assertNull(repository.get());
        assertTrue(gameView.getPlayerNames().isEmpty());
        assertEquals("", gameOverView.getWinnerName());
        assertTrue(startMenu.getPlayerNames().isEmpty());

        startGame.startGame(List.of("Cat", "Dan", "Eve"), "London");
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertEquals("Cat", gameView.getCurrentPlayerName());
        assertEquals(3, game().getPlayers().size());
    }

    @Test
    void aWeatherFailureStillStartsTheGameAndSaysSo() {
        weatherDown = true;
        startGame.startGame(List.of("Ann", "Bob"), "Tokyo");
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertEquals("Weather unavailable: normal resource production", startMenu.getWeatherText());
        assertEquals("Bonus: none", startMenu.getBonusText());
    }

    @Test
    void returningToTheMenuDoesNothingWhileAGameIsStillOn() {
        startGame.startGame(List.of("Ann", "Bob"), "Toronto");
        Game before = game();
        restart.returnToMenu();
        assertEquals("The game is not over yet.", gameOverView.getErrorMessage());
        assertSame(before, game());
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
    }
}
