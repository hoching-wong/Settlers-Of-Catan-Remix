package catan.app;

import catan.entity.CityLocation;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.ui.swing.GameOverView;
import catan.framework.ui.swing.GameView;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.framework.ui.swing.StartMenuView;
import catan.framework.weather.OpenMeteoWeatherDataAccessObject;
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
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.endturn.EndTurnInteractor;
import catan.usecase.placeinitial.PlaceInitialInteractor;
import catan.usecase.port.GameRepository;
import catan.usecase.weather.WeatherGateway;
import catan.usecase.restartgame.RestartGameInteractor;
import catan.usecase.startgame.StartGameInteractor;
import java.awt.Dimension;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;

/**
 * The only place where the concrete classes are created and
 * connected to each other. Everything else depends on interfaces.
 */
public class AppBuilder {

    private final Random random;
    private final WeatherGateway weatherGateway;
    private final GameRepository gameRepository;
    private final Executor backgroundExecutor;

    private GameView gameView;
    private PlaceInitialController placeInitialController;
    private GameOverPresenter gameOverPresenter;

    /**
     * Creates the application with the Open-Meteo weather service.
     */
    public AppBuilder() {
        this(new Random(), new OpenMeteoWeatherDataAccessObject(),
                new InMemoryGameRepository(), daemonExecutor());
    }

    /**
     * Creates the application with configurable dependencies.
     *
     * @param random the random number generator
     * @param weatherGateway the weather service
     * @param gameRepository the game repository
     * @param backgroundExecutor the background executor
     */
    public AppBuilder(Random random, WeatherGateway weatherGateway,
                      GameRepository gameRepository, Executor backgroundExecutor) {
        this.random = Objects.requireNonNull(random, "random");
        this.weatherGateway = Objects.requireNonNull(
                weatherGateway, "weatherGateway");
        this.gameRepository = Objects.requireNonNull(
                gameRepository, "gameRepository");
        this.backgroundExecutor = Objects.requireNonNull(
                backgroundExecutor, "backgroundExecutor");
    }

    /**
     * Creates every screen and connects the components.
     *
     * @return the screen switcher
     */
    public ScreenSwitcher buildScreens() {
        StartMenuViewModel startMenu = new StartMenuViewModel(
                CityLocation.defaults().stream()
                        .map(CityLocation::getName)
                        .toList());

        GameViewModel game = new GameViewModel();
        GameOverViewModel gameOver = new GameOverViewModel();
        ViewManagerModel viewManager = new ViewManagerModel();

        StartGameController startGame = new StartGameController(
                new StartGameInteractor(
                        gameRepository,
                        weatherGateway,
                        new StartGamePresenter(startMenu, game, viewManager),
                        random));

        placeInitialController = new PlaceInitialController(
                new PlaceInitialInteractor(
                        gameRepository,
                        new PlaceInitialPresenter(game)));

        EndTurnController endTurn = new EndTurnController(
                new EndTurnInteractor(
                        gameRepository,
                        new EndTurnPresenter(game)));

        RestartGameController restartGame = new RestartGameController(
                new RestartGameInteractor(
                        gameRepository,
                        new RestartGamePresenter(
                                startMenu, game, gameOver, viewManager)));

        gameOverPresenter = new GameOverPresenter(gameOver, viewManager);

        StartMenuView startMenuView = new StartMenuView(
                startMenu, startGame, backgroundExecutor);

        gameView = new GameView(game, endTurn);

        GameOverView gameOverView = new GameOverView(
                gameOver, restartGame);

        return new ScreenSwitcher(
                viewManager, startMenuView, gameView, gameOverView);
    }

    /**
     * Creates the application window.
     *
     * @return the application frame
     */
    public JFrame buildWindow() {
        JFrame frame = new JFrame("Settlers of Catan Remix");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(buildScreens());
        frame.setMinimumSize(new Dimension(640, 560));
        frame.setPreferredSize(new Dimension(960, 680));
        frame.pack();
        frame.setLocationRelativeTo(null);
        return frame;
    }

    /**
     * Returns the game view after the screens are built.
     *
     * @return the game view
     */
    public GameView getGameView() {
        return requireBuilt(gameView);
    }

    /**
     * Returns the initial placement controller.
     *
     * @return the initial placement controller
     */
    public PlaceInitialController getPlaceInitialController() {
        return requireBuilt(placeInitialController);
    }

    /**
     * Returns the game over presenter.
     *
     * @return the game over presenter
     */
    public GameOverPresenter getGameOverPresenter() {
        return requireBuilt(gameOverPresenter);
    }

    /**
     * Ensures that a component has been initialized.
     *
     * @param part the component
     * @param <T> the component type
     * @return the initialized component
     */
    private static <T> T requireBuilt(T part) {
        if (part == null) {
            throw new IllegalStateException("Call buildScreens() first");
        }
        return part;
    }

    /**
     * Creates a single-threaded daemon executor.
     *
     * @return the executor service
     */
    private static ExecutorService daemonExecutor() {
        return Executors.newSingleThreadExecutor(task -> {
            Thread thread = new Thread(task, "start-game");
            thread.setDaemon(true);
            return thread;
        });
    }
}
