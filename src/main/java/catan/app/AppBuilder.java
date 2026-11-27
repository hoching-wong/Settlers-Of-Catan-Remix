package catan.app;

import catan.entity.BalancedLayoutStrategy;
import catan.entity.CityLocation;
import catan.framework.persistence.InMemoryBoardRepository;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.ui.swing.BoardView;
import catan.framework.ui.swing.GameOverView;
import catan.framework.ui.swing.GameView;
import catan.framework.ui.swing.InitialPlacementClickListener;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.framework.ui.swing.StartMenuView;
import catan.framework.weather.OpenMeteoWeatherDataAccessObject;
import catan.interfaceadapter.controller.BuildController;
import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.controller.GenerateBoardController;
import catan.interfaceadapter.controller.PlaceInitialController;
import catan.interfaceadapter.controller.RestartGameController;
import catan.interfaceadapter.controller.SelectStructureController;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.presenter.BuildPresenter;
import catan.interfaceadapter.presenter.EndTurnPresenter;
import catan.interfaceadapter.presenter.GameOverPresenter;
import catan.interfaceadapter.presenter.GenerateBoardPresenter;
import catan.interfaceadapter.presenter.LongestRoadPresenter;
import catan.interfaceadapter.presenter.PlaceInitialPresenter;
import catan.interfaceadapter.presenter.RestartGamePresenter;
import catan.interfaceadapter.presenter.SelectStructurePresenter;
import catan.interfaceadapter.presenter.StartGamePresenter;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.build.BuildInteractor;
import catan.usecase.endturn.EndTurnInteractor;
import catan.usecase.generateboard.GenerateBoardInteractor;
import catan.usecase.longestroad.LongestRoadInputBoundary;
import catan.usecase.longestroad.LongestRoadInteractor;
import catan.usecase.placeinitial.PlaceInitialInteractor;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;
import catan.usecase.restartgame.RestartGameInteractor;
import catan.usecase.selectstructure.SelectStructureInteractor;
import catan.usecase.startgame.StartGameInteractor;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherInputBoundary;
import catan.usecase.weather.WeatherInteractor;
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
    private final WeatherDataAccessInterface weatherDataAccessObject;
    private final GameRepository gameRepository;
    private final Executor backgroundExecutor;

    private GameView gameView;
    private BoardViewModel boardViewModel;
    private PlaceInitialController placeInitialController;
    private LongestRoadViewModel longestRoadViewModel;
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
     * @param weatherDataAccessObject the weather data access object
     * @param gameRepository the game repository
     * @param backgroundExecutor the background executor
     */
    public AppBuilder(
            Random random,
            WeatherDataAccessInterface weatherDataAccessObject,
            GameRepository gameRepository,
            Executor backgroundExecutor) {

        this.random = Objects.requireNonNull(random, "random");
        this.weatherDataAccessObject = Objects.requireNonNull(
                weatherDataAccessObject, "weatherDataAccessObject");
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
        longestRoadViewModel = new LongestRoadViewModel();
        boardViewModel = new BoardViewModel();
        BoardRepository boardRepository = new InMemoryBoardRepository();

        WeatherInputBoundary weatherInteractor =
                new WeatherInteractor(weatherDataAccessObject);

        // UC8: Start Game generates the board, and the game is then played on that same board.
        GenerateBoardController generateBoard = new GenerateBoardController(new GenerateBoardInteractor(
                gameRepository, boardRepository, new GenerateBoardPresenter(boardViewModel),
                new BalancedLayoutStrategy()));

        StartGameController startGame = new StartGameController(new StartGameInteractor(
                gameRepository, weatherInteractor, new StartGamePresenter(startMenu, game, viewManager,
                boardViewModel, longestRoadViewModel),
                random, boardRepository));

        placeInitialController = new PlaceInitialController(
                new PlaceInitialInteractor(gameRepository, new PlaceInitialPresenter(game, boardViewModel)));

        // UC1: the build menu highlights where a structure can go, and a click there builds it.
        SelectStructureController selectStructure = new SelectStructureController(
                new SelectStructureInteractor(gameRepository, new SelectStructurePresenter(boardViewModel)));

        LongestRoadInputBoundary longestRoadInteractor =
                new LongestRoadInteractor();

        LongestRoadPresenter longestRoadPresenter =
                new LongestRoadPresenter(longestRoadViewModel);

        BuildController build = new BuildController(
                new BuildInteractor(gameRepository,
                        new BuildPresenter(boardViewModel, longestRoadPresenter),
                        longestRoadInteractor));

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

        StartMenuView startMenuView = new StartMenuView(startMenu, startGame, backgroundExecutor);
        startMenuView.setGenerateBoardController(generateBoard);

        BoardView boardView = new BoardView(boardViewModel);
        boardView.setSelectStructureController(selectStructure);
        boardView.setBuildController(build);
        boardView.getBoardPanel().addClickListener(
                new InitialPlacementClickListener(boardViewModel, placeInitialController));

        gameView = new GameView(game, endTurn, longestRoadViewModel);
        gameView.setBoardComponent(boardView);
        GameOverView gameOverView = new GameOverView(gameOver, restartGame);

        return new ScreenSwitcher(viewManager, startMenuView, gameView, gameOverView);
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
        frame.setMinimumSize(new Dimension(720, 680));
        frame.setPreferredSize(new Dimension(1000, 860));
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

    /** What presenters that change the board update so the board view redraws. */
    public BoardViewModel getBoardViewModel() {
        return requireBuilt(boardViewModel);
    }

    /** What the board view calls when a vertex or edge is clicked during setup. */
    public PlaceInitialController getPlaceInitialController() {
        return requireBuilt(placeInitialController);
    }

    /**
     * Returns the view model containing Longest Road information.
     *
     * @return the Longest Road view model
     */
    public LongestRoadViewModel getLongestRoadViewModel() {
        return requireBuilt(longestRoadViewModel);
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
            throw new IllegalStateException(
                    "Call buildScreens() first");
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
