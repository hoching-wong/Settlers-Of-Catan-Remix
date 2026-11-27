package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.usecase.longestroad.LongestRoadInteractor;
import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.StructureType;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.framework.persistence.InMemoryGameRepository;
import catan.interfaceadapter.controller.ViewGameStatusController;
import catan.interfaceadapter.presenter.BuildPresenter;
import catan.interfaceadapter.presenter.EndTurnPresenter;
import catan.interfaceadapter.presenter.PlaceInitialPresenter;
import catan.interfaceadapter.presenter.StartGamePresenter;
import catan.interfaceadapter.presenter.ViewGameStatusPresenter;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.TestGames;
import catan.usecase.build.BuildInputData;
import catan.usecase.build.BuildInteractor;
import catan.usecase.build.placement.PlacementStrategies;
import catan.usecase.endturn.EndTurnInputData;
import catan.usecase.endturn.EndTurnInteractor;
import catan.usecase.placeinitial.PlaceInitialInputData;
import catan.usecase.placeinitial.PlaceInitialInteractor;
import catan.usecase.startgame.StartGameOutputData;
import catan.usecase.viewgamestatus.ViewGameStatusInteractor;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Integration tests for the game status update flow. */
class GameStatusIntegrationTest {
  private InMemoryGameRepository repository;
  private GameViewModel gameViewModel;
  private BoardViewModel boardViewModel;
  private StartMenuViewModel startMenuViewModel;
  private GameStatusViewModel statusViewModel;
  private GameStatusPanel statusPanel;
  private ViewGameStatusController statusController;
  private Player alice;
  private int refreshCount;

  /** Connects the game status components before each test. */
  @BeforeEach
  void setUp() {
    repository = new InMemoryGameRepository();
    gameViewModel = new GameViewModel();
    boardViewModel = new BoardViewModel();
    statusViewModel = new GameStatusViewModel();
    refreshCount = 0;

    ViewGameStatusPresenter presenter =
            new ViewGameStatusPresenter(statusViewModel);

    ViewGameStatusInteractor interactor =
            new ViewGameStatusInteractor(repository, presenter);

    statusController = new ViewGameStatusController(interactor);

    startMenuViewModel = new StartMenuViewModel(List.of("Toronto"));

    statusPanel = new GameStatusPanel(statusViewModel, startMenuViewModel);

    alice = new Player("Alice");
    Player bob = new Player("Bob");

    alice.getResources().addResource(ResourceType.LUMBER, 3);
    alice.getResources().addResource(ResourceType.BRICK, 2);

    Game game =
            new Game(
                    BoardFactory.createStandardBoard(new Random(1)),
                    List.of(alice, bob),
                    WeatherData.fromWmoCode(0),
                    new Dice(new Random(1)));

    game.startGame();
    repository.save(game);
  }

  /** Requests the latest game status and records the refresh. */
  private void refreshGameStatus() {
    refreshCount++;
    statusController.execute();
  }

  /**
   * Tests that the start game presenter displays the initial game status.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void gameStartUpdatesStatusPanel() throws Exception {
    StartGamePresenter presenter =
            new StartGamePresenter(
                    startMenuViewModel,
                    gameViewModel,
                    new ViewManagerModel(),
                    boardViewModel,
                    this::refreshGameStatus);

    presenter.presentError("Invalid player names.");

    assertEquals(0, refreshCount);

    WeatherData weather = repository.get().getWeather();

    presenter.presentGameStarted(
            new StartGameOutputData(
                    List.of("Alice", "Bob"),
                    "Toronto",
                    true,
                    weather.getCondition(),
                    weather.getBonusResource(),
                    weather.getMultiplier()));

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(1, refreshCount);
    assertEquals(2, statusViewModel.getPlayers().size());
    assertEquals("Alice", statusViewModel.getCurrentPlayerName());
    assertEquals("Weather: Sunny", statusViewModel.getWeatherText());
    assertEquals("Bonus: Lumber x2", statusViewModel.getBonusText());

    List<String> labels = getLabelTexts(statusPanel);

    assertTrue(labels.contains("Game Overview"));
    assertTrue(labels.contains("Sunny"));
    assertTrue(labels.contains("Weather Bonus"));
    assertTrue(labels.contains("Lumber ×2"));

    Component aliceCard =
            findNamedComponent(statusPanel, "player-card-Alice");

    assertNotNull(aliceCard);

    List<String> aliceLabels = getLabelTexts(aliceCard);

    assertTrue(aliceLabels.contains("Alice"));
    assertTrue(aliceLabels.contains("Current Turn"));
    assertTrue(aliceLabels.contains("Victory Points"));

    assertLabelValue(aliceCard, "victory-points-value", "0");

    assertResourceCount(aliceCard, "LUMBER", "Lumber", 3);
    assertResourceCount(aliceCard, "BRICK", "Brick", 2);
  }

  /**
   * Tests that a successful build refreshes the game status panel.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void resourceChangeRefreshesStatusPanel() throws Exception {
    Game game = TestGames.playingGame(4, "Alice", "Bob");

    game.markRolled();

    alice = game.getCurrentPlayer();

    alice.getResources().addResource(ResourceType.LUMBER, 3);
    alice.getResources().addResource(ResourceType.BRICK, 2);

    repository.save(game);
    statusController.execute();

    int lumberBefore =
            alice.getResources().getResourceCount(ResourceType.LUMBER);

    int brickBefore =
            alice.getResources().getResourceCount(ResourceType.BRICK);

    BuildPresenter presenter =
            new BuildPresenter(
                    boardViewModel, this::refreshGameStatus);

    BuildInteractor interactor =
            new BuildInteractor(
                    repository,
                    presenter,
                    new LongestRoadInteractor());

    int edgeId =
            PlacementStrategies.standard()
                    .get(StructureType.ROAD)
                    .validLocations(game.getBoard(), alice)
                    .get(0);

    interactor.execute(
            new BuildInputData(StructureType.ROAD, edgeId));

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(1, refreshCount);

    assertEquals(
            lumberBefore - 1,
            statusViewModel.getPlayers().get(0).resources().get("LUMBER"));

    assertEquals(
            brickBefore - 1,
            statusViewModel.getPlayers().get(0).resources().get("BRICK"));

    Component aliceCard =
            findNamedComponent(statusPanel, "player-card-Alice");

    assertNotNull(aliceCard);

    assertResourceCount(
            aliceCard, "LUMBER", "Lumber", lumberBefore - 1);

    assertResourceCount(
            aliceCard, "BRICK", "Brick", brickBefore - 1);
  }

  /** Tests that a failed build does not refresh the game status. */
  @Test
  void failedBuildDoesNotRefreshStatus() {
    Game game = TestGames.playingGame(4, "Alice", "Bob");

    game.markRolled();
    TestGames.give(game.getCurrentPlayer(), 5);
    repository.save(game);

    statusController.execute();

    List<GameStatusViewModel.PlayerStatus> previousStatus =
            statusViewModel.getPlayers();

    BuildPresenter presenter =
            new BuildPresenter(
                    boardViewModel, this::refreshGameStatus);

    BuildInteractor interactor =
            new BuildInteractor(
                    repository,
                    presenter,
                    new LongestRoadInteractor());

    interactor.execute(
            new BuildInputData(StructureType.ROAD, -1));

    assertTrue(boardViewModel.getState().isError());
    assertEquals(0, refreshCount);
    assertEquals(previousStatus, statusViewModel.getPlayers());
  }

  /**
   * Tests that placing an initial settlement updates victory points.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void initialPlacementRefreshesVictoryPoints() throws Exception {
    Game game = repository.get();

    statusController.execute();

    assertEquals(0,
            statusViewModel.getPlayers().get(0).victoryPoints());

    PlaceInitialPresenter presenter =
            new PlaceInitialPresenter(
                    gameViewModel,
                    boardViewModel,
                    this::refreshGameStatus);

    PlaceInitialInteractor interactor =
            new PlaceInitialInteractor(repository, presenter);

    interactor.placeInitial(new PlaceInitialInputData(-1));

    assertEquals(0, refreshCount);

    Vertex vertex =
            game.getBoard().getVertices().stream()
                    .filter(v ->
                            game.getBoard().isValidSettlementLocation(v, alice, true))
                    .findFirst()
                    .orElseThrow();

    interactor.placeInitial(
            new PlaceInitialInputData(vertex.getId()));

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(1, refreshCount);

    assertEquals(
            1, statusViewModel.getPlayers().get(0).victoryPoints());

    Component aliceCard =
            findNamedComponent(statusPanel, "player-card-Alice");

    assertNotNull(aliceCard);

    assertLabelValue(aliceCard, "victory-points-value", "1");
  }

  /**
   * Tests that ending a turn refreshes the current player.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void endTurnRefreshesCurrentPlayer() throws Exception {
    Game game = TestGames.playingGame(4, "Alice", "Bob");

    repository.save(game);
    statusController.execute();

    assertEquals("Alice", statusViewModel.getCurrentPlayerName());

    EndTurnPresenter presenter =
            new EndTurnPresenter(
                    gameViewModel, this::refreshGameStatus);

    EndTurnInteractor interactor =
            new EndTurnInteractor(repository, presenter);

    interactor.endTurn(new EndTurnInputData());

    assertEquals(0, refreshCount);

    game.markRolled();

    interactor.endTurn(new EndTurnInputData());

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(1, refreshCount);
    assertEquals("Bob", statusViewModel.getCurrentPlayerName());

    Component bobCard =
            findNamedComponent(statusPanel, "player-card-Bob");

    assertNotNull(bobCard);
    assertTrue(getLabelTexts(bobCard).contains("Current Turn"));
  }

  /**
   * Checks the resource name and count in a player's resource block.
   *
   * @param card the player card
   * @param resource the resource identifier
   * @param displayName the displayed resource name
   * @param count the expected resource count
   */
  private static void assertResourceCount(
          Component card, String resource, String displayName, int count) {

    Component block = findNamedComponent(card, "resource-" + resource);

    assertNotNull(block);
    assertTrue(getLabelTexts(block).contains(displayName));

    assertLabelValue(block, "resource-count", String.valueOf(count));
  }

  /**
   * Checks the text of a named label.
   *
   * @param parent the parent component
   * @param name the component name
   * @param expected the expected label text
   */
  private static void assertLabelValue(
          Component parent, String name, String expected) {

    Component component = findNamedComponent(parent, name);

    assertTrue(component instanceof JLabel);
    assertEquals(expected, ((JLabel) component).getText());
  }

  /**
   * Finds a named component recursively within a component tree.
   *
   * @param component the component to search
   * @param name the component name
   * @return the matching component, or null if not found
   */
  private static Component findNamedComponent(
          Component component, String name) {

    if (name.equals(component.getName())) {
      return component;
    }

    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        Component found = findNamedComponent(child, name);

        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  /**
   * Collects the text of all labels inside a Swing component.
   *
   * @param component the component to search
   * @return the collected label texts
   */
  private static List<String> getLabelTexts(Component component) {

    List<String> texts = new ArrayList<>();

    collectLabelTexts(component, texts);

    return texts;
  }

  /**
   * Recursively collects label text from nested components.
   *
   * @param component the component to search
   * @param texts the list containing collected label texts
   */
  private static void collectLabelTexts(
          Component component, List<String> texts) {

    if (component instanceof JLabel label) {
      texts.add(label.getText());
    }

    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        collectLabelTexts(child, texts);
      }
    }
  }
}
