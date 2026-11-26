package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.WeatherData;
import catan.framework.persistence.InMemoryGameRepository;
import catan.interfaceadapter.controller.ViewGameStatusController;
import catan.interfaceadapter.presenter.ViewGameStatusPresenter;
import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
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
  private GameStatusViewModel statusViewModel;
  private GameStatusPanel statusPanel;
  private Player alice;

  /** Connects the game status components before each test. */
  @BeforeEach
  void setUp() {
    repository = new InMemoryGameRepository();
    gameViewModel = new GameViewModel();
    statusViewModel = new GameStatusViewModel();

    ViewGameStatusPresenter presenter = new ViewGameStatusPresenter(statusViewModel);

    ViewGameStatusInteractor interactor = new ViewGameStatusInteractor(repository, presenter);

    ViewGameStatusController controller = new ViewGameStatusController(interactor);

    StartMenuViewModel startMenuViewModel = new StartMenuViewModel(List.of("Toronto"));

    statusPanel = new GameStatusPanel(statusViewModel, startMenuViewModel);

    gameViewModel.addPropertyChangeListener(event -> {
      String property = event.getPropertyName();

      if (GameViewModel.PLAYERS.equals(property)
          || GameViewModel.TURN.equals(property)
          || GameViewModel.MESSAGE.equals(property)) {
        controller.execute();
      }
    });

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

  /**
   * Tests that starting a game displays the initial game status.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void gameStartUpdatesStatusPanel() throws Exception {
    gameViewModel.startGame(List.of("Alice", "Bob"), "Alice", "Place a settlement");

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(2, statusViewModel.getPlayers().size());
    assertEquals("Alice", statusViewModel.getCurrentPlayerName());
    assertEquals("Weather: Sunny", statusViewModel.getWeatherText());
    assertEquals("Bonus: Lumber x2", statusViewModel.getBonusText());

    List<String> labels = getLabelTexts(statusPanel);

    assertTrue(labels.contains("Game Overview"));
    assertTrue(labels.contains("Sunny"));
    assertTrue(labels.contains("Weather Bonus"));
    assertTrue(labels.contains("Lumber ×2"));

    Component aliceCard = findNamedComponent(statusPanel, "player-card-Alice");

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
   * Tests that a resource change refreshes the game status panel.
   *
   * @throws Exception if the Swing event thread operation fails
   */
  @Test
  void resourceChangeRefreshesStatusPanel() throws Exception {
    gameViewModel.startGame(List.of("Alice", "Bob"), "Alice", "Place a settlement");

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(3, statusViewModel.getPlayers().get(0).resources().get("LUMBER"));

    // Simulate the resource cost of building a road.
    alice.getResources().removeResource(ResourceType.LUMBER, 1);
    alice.getResources().removeResource(ResourceType.BRICK, 1);

    // Simulate a presenter notifying the game ViewModel.
    gameViewModel.setMessage("Alice built a road.");

    SwingUtilities.invokeAndWait(() -> {});

    assertEquals(2, statusViewModel.getPlayers().get(0).resources().get("LUMBER"));

    assertEquals(1, statusViewModel.getPlayers().get(0).resources().get("BRICK"));

    Component aliceCard = findNamedComponent(statusPanel, "player-card-Alice");

    assertNotNull(aliceCard);

    assertResourceCount(aliceCard, "LUMBER", "Lumber", 2);
    assertResourceCount(aliceCard, "BRICK", "Brick", 1);
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
  private static void assertLabelValue(Component parent, String name, String expected) {

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
  private static Component findNamedComponent(Component component, String name) {

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
  private static void collectLabelTexts(Component component, List<String> texts) {

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
