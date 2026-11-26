package catan.app;

import catan.entity.BalancedLayoutStrategy;
import catan.entity.CityLocation;
import catan.framework.persistence.InMemoryBoardRepository;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.ui.swing.BoardView;
import catan.framework.ui.swing.GameOverView;
import catan.framework.ui.swing.GameStatusPanel;
import catan.framework.ui.swing.GameView;
import catan.framework.ui.swing.InitialPlacementClickListener;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.framework.ui.swing.StartMenuView;
import catan.interfaceadapter.controller.BuildController;
import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.controller.GenerateBoardController;
import catan.interfaceadapter.controller.PlaceInitialController;
import catan.interfaceadapter.controller.RestartGameController;
import catan.interfaceadapter.controller.SelectStructureController;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.controller.ViewGameStatusController;
import catan.interfaceadapter.presenter.BuildPresenter;
import catan.interfaceadapter.presenter.EndTurnPresenter;
import catan.interfaceadapter.presenter.GameOverPresenter;
import catan.interfaceadapter.presenter.GenerateBoardPresenter;
import catan.interfaceadapter.presenter.PlaceInitialPresenter;
import catan.interfaceadapter.presenter.RestartGamePresenter;
import catan.interfaceadapter.presenter.SelectStructurePresenter;
import catan.interfaceadapter.presenter.StartGamePresenter;
import catan.interfaceadapter.presenter.ViewGameStatusPresenter;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.build.BuildInteractor;
import catan.usecase.endturn.EndTurnInteractor;
import catan.usecase.generateboard.GenerateBoardInteractor;
import catan.usecase.placeinitial.PlaceInitialInteractor;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;
import catan.usecase.port.WeatherGateway;
import catan.usecase.port.WeatherUnavailableException;
import catan.usecase.restartgame.RestartGameInteractor;
import catan.usecase.selectstructure.SelectStructureInteractor;
import catan.usecase.startgame.StartGameInteractor;
import catan.usecase.viewgamestatus.ViewGameStatusInteractor;
import java.awt.Dimension;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.swing.JFrame;

/**
 * The only place where the concrete classes are created
 * and connected to each other.
 * Everything else depends on interfaces.
 */
public class AppBuilder {

    private final Random random;
    private final WeatherGateway weatherGateway;
    private final GameRepository gameRepository;
    private final Executor backgroundExecutor;

    private GameView gameView;
    private BoardViewModel boardViewModel;
    private PlaceInitialController placeInitialController;
    private GameOverPresenter gameOverPresenter;

    /** The application as it runs. */
    public AppBuilder() {
        this(new Random(), new WeatherNotConnected(),
                new InMemoryGameRepository(), daemonExecutor());
    }

    /** For tests and for plugging in the real weather gateway. */
    public AppBuilder(
            Random random,
            WeatherGateway weatherGateway,
            GameRepository gameRepository,
            Executor backgroundExecutor) {

        this.random = Objects.requireNonNull(random, "random");
        this.weatherGateway = Objects.requireNonNull(
                weatherGateway, "weatherGateway");
        this.gameRepository = Objects.requireNonNull(
                gameRepository, "gameRepository");
        this.backgroundExecutor = Objects.requireNonNull(
                backgroundExecutor, "backgroundExecutor");
    }

    /** Creates every screen and connects them. */
    public ScreenSwitcher buildScreens() {

        StartMenuViewModel startMenu = new StartMenuViewModel(
                CityLocation.defaults().stream()
                        .map(CityLocation::getName)
                        .toList());

        GameViewModel game = new GameViewModel();
        GameOverViewModel gameOver = new GameOverViewModel();
        ViewManagerModel viewManager = new ViewManagerModel();

        boardViewModel = new BoardViewModel();
        BoardRepository boardRepository =
                new InMemoryBoardRepository();

        // UC5: View Game Status.
        GameStatusViewModel statusViewModel =
                new GameStatusViewModel();

        ViewGameStatusPresenter statusPresenter =
                new ViewGameStatusPresenter(statusViewModel);

        ViewGameStatusController statusController =
                new ViewGameStatusController(
                        new ViewGameStatusInteractor(
                                gameRepository, statusPresenter));

        GameStatusPanel statusPanel =
                new GameStatusPanel(statusViewModel, startMenu);

        // UC8: Generate the board used by the game.
        GenerateBoardController generateBoard =
                new GenerateBoardController(
                        new GenerateBoardInteractor(
                                gameRepository,
                                boardRepository,
                                new GenerateBoardPresenter(boardViewModel),
                                new BalancedLayoutStrategy()));

        StartGameController startGame =
                new StartGameController(
                        new StartGameInteractor(
                                gameRepository,
                                weatherGateway,
                                new StartGamePresenter(
                                        startMenu,
                                        game,
                                        viewManager,
                                        boardViewModel,
                                        statusController::execute),
                                random,
                                boardRepository));

        placeInitialController =
                new PlaceInitialController(
                        new PlaceInitialInteractor(
                                gameRepository,
                                new PlaceInitialPresenter(
                                        game,
                                        boardViewModel,
                                        statusController::execute)));

        // UC1: Select and build structures.
        SelectStructureController selectStructure =
                new SelectStructureController(
                        new SelectStructureInteractor(
                                gameRepository,
                                new SelectStructurePresenter(
                                        boardViewModel)));

        BuildController build =
                new BuildController(
                        new BuildInteractor(
                                gameRepository,
                                new BuildPresenter(
                                        boardViewModel,
                                        statusController::execute)));

        EndTurnController endTurn =
                new EndTurnController(
                        new EndTurnInteractor(
                                gameRepository,
                                new EndTurnPresenter(
                                        game,
                                        statusController::execute)));

        RestartGameController restartGame =
                new RestartGameController(
                        new RestartGameInteractor(
                                gameRepository,
                                new RestartGamePresenter(
                                        startMenu,
                                        game,
                                        gameOver,
                                        viewManager)));

        gameOverPresenter =
                new GameOverPresenter(gameOver, viewManager);

        StartMenuView startMenuView =
                new StartMenuView(
                        startMenu, startGame, backgroundExecutor);

        startMenuView.setGenerateBoardController(generateBoard);

        BoardView boardView = new BoardView(boardViewModel);

        boardView.setSelectStructureController(selectStructure);
        boardView.setBuildController(build);

        boardView.getBoardPanel().addClickListener(
                new InitialPlacementClickListener(
                        boardViewModel, placeInitialController));

        gameView = new GameView(game, endTurn);

        // Connect the board and the game status panel.
        gameView.setBoardComponent(boardView);
        gameView.setGameStatusPanel(statusPanel);

        GameOverView gameOverView =
                new GameOverView(gameOver, restartGame);

        return new ScreenSwitcher(
                viewManager,
                startMenuView,
                gameView,
                gameOverView);
    }

    /** The application window. Needs a display. */
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

    /** Returns the game view after the screens have been built. */
    public GameView getGameView() {
        return requireBuilt(gameView);
    }

    /** Returns the board view model. */
    public BoardViewModel getBoardViewModel() {
        return requireBuilt(boardViewModel);
    }

    /** Returns the controller for initial structure placement. */
    public PlaceInitialController getPlaceInitialController() {
        return requireBuilt(placeInitialController);
    }

    /** Returns the presenter for the game over screen. */
    public GameOverPresenter getGameOverPresenter() {
        return requireBuilt(gameOverPresenter);
    }

    private static <T> T requireBuilt(T part) {

        if (part == null) {
            throw new IllegalStateException(
                    "Call buildScreens() first");
        }

        return part;
    }

    private static ExecutorService daemonExecutor() {

        return Executors.newSingleThreadExecutor(task -> {

            Thread thread = new Thread(task, "start-game");
            thread.setDaemon(true);

            return thread;
        });
    }

    /**
     * Stand-in until the real weather service is connected.
     */
    private static final class WeatherNotConnected
            implements WeatherGateway {

        @Override
        public int fetchWeatherCode(CityLocation city)
                throws WeatherUnavailableException {

            throw new WeatherUnavailableException(
                    "The weather service is not connected yet");
        }
    }
}
