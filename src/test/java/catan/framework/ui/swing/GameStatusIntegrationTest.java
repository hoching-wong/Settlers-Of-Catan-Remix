package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

  /** Connects the UC5 components before each test. */
  @BeforeEach
  void setUp() {
    repository = new InMemoryGameRepository();
    gameViewModel = new GameViewModel();
    statusViewModel = new GameStatusViewModel();

    ViewGameStatusPresenter presenter = new ViewGameStatusPresenter(statusViewModel);

    ViewGameStatusInteractor interactor = new ViewGameStatusInteractor(repository, presenter);

    ViewGameStatusController controller = new ViewGameStatusController(interactor);

    statusPanel = new GameStatusPanel(statusViewModel);

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
   * Tests that starting a game displays the initial status.
   *
   * @throws Exception if the Swing event thread is interrupted
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

    assertTrue(labels.contains("Alice (Current Turn)"));
    assertTrue(labels.contains("Victory Points: 0"));
    assertTrue(labels.contains("LUMBER: 3"));
    assertTrue(labels.contains("BRICK: 2"));
    assertTrue(labels.contains("Weather: Sunny"));
    assertTrue(labels.contains("Bonus: Lumber x2"));
  }

  /**
   * Tests that changing resources and sending a game event refreshes the status panel
   * automatically.
   *
   * @throws Exception if the Swing event thread is interrupted
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

    List<String> labels = getLabelTexts(statusPanel);

    assertTrue(labels.contains("LUMBER: 2"));
    assertTrue(labels.contains("BRICK: 1"));
  }

  /**
   * Collects the text of all labels inside a Swing component.
   *
   * @param component the component to inspect
   * @return the text displayed by its labels
   */
  private static List<String> getLabelTexts(Component component) {
    List<String> texts = new ArrayList<>();
    collectLabelTexts(component, texts);
    return texts;
  }

  /**
   * Recursively collects label text from nested components.
   *
   * @param component the current component
   * @param texts the list receiving label text
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
