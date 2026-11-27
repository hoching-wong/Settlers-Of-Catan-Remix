package catan.usecase.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.Board;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.Road;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.framework.ui.swing.LongestRoadPanel;
import catan.interfaceadapter.presenter.LongestRoadPresenter;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Integration tests for the Longest Road use case. */
class LongestRoadIntegrationTest {

  private final Player alice = new Player("Alice");
  private final Player bob = new Player("Bob");

  private final Board board = createBoard();

  private final Game game = new Game(
          board,
          List.of(alice, bob),
          WeatherData.none(),
          new Dice(new Random(1)));

  private final LongestRoadViewModel viewModel =
          new LongestRoadViewModel();

  private final LongestRoadPresenter presenter =
          new LongestRoadPresenter(viewModel);

  private final LongestRoadInteractor interactor =
          new LongestRoadInteractor();

  /** Recalculates the Longest Road and presents the result. */
  private void updateLongestRoad() {
    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(game));

    presenter.present(outputData);
  }

  /**
   * Creates two separate paths with six edges each.
   *
   * @return a board for integration testing
   */
  private static Board createBoard() {
    List<Vertex> vertices = new ArrayList<>();
    List<Edge> edges = new ArrayList<>();

    for (int i = 0; i < 15; i++) {
      vertices.add(new Vertex(i));
    }

    for (int i = 0; i < 6; i++) {
      edges.add(new Edge(
              i,
              vertices.get(i),
              vertices.get(i + 1)));
    }

    for (int i = 8; i < 14; i++) {
      edges.add(new Edge(
              i,
              vertices.get(i),
              vertices.get(i + 1)));
    }

    return new Board(List.of(), vertices, edges);
  }

  /**
   * Places consecutive roads on the test board.
   *
   * @param player the owner
   * @param start the first edge ID
   * @param count the number of roads
   */
  private void addRoads(Player player, int start, int count) {
    for (int i = start; i < start + count; i++) {
      Edge edge = board.getEdge(i);
      board.placeRoad(edge, new Road(player, edge));
    }
  }

  /**
   * Creates the panel on the Swing event dispatch thread.
   *
   * @return the panel
   * @throws Exception if Swing initialization fails
   */
  private LongestRoadPanel createPanel() throws Exception {
    AtomicReference<LongestRoadPanel> result =
            new AtomicReference<>();

    SwingUtilities.invokeAndWait(
            () -> result.set(new LongestRoadPanel(viewModel)));

    return result.get();
  }

  /**
   * Waits for queued Swing updates to finish.
   *
   * @throws Exception if Swing processing fails
   */
  private void flushSwingEvents() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      // Wait for previous Swing events.
    });
  }

  /**
   * Finds a named component recursively within the panel.
   *
   * @param component the component to inspect
   * @param name the expected component name
   * @return the matching component, or null if not found
   */
  private Component findNamedComponent(Component component, String name) {
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
   * Checks the displayed text of a named label.
   *
   * @param panel the panel to inspect
   * @param name the expected component name
   * @param expected the expected label text
   */
  private void assertLabelValue(
          LongestRoadPanel panel, String name, String expected) {

    Component component = findNamedComponent(panel, name);

    assertNotNull(component);
    assertTrue(component instanceof JLabel);
    assertEquals(expected, ((JLabel) component).getText());
  }

  /**
   * Checks the displayed Longest Road information.
   *
   * @param panel the panel to inspect
   * @param holder the expected holder
   * @param length the expected road length
   * @param bonus the expected victory point bonus
   */
  private void assertLongestRoadStatus(
          LongestRoadPanel panel, String holder, String length, String bonus) {

    assertLabelValue(panel, "longest-road-holder", holder);
    assertLabelValue(panel, "longest-road-length", length);
    assertLabelValue(panel, "longest-road-bonus", bonus);
  }

  @Test
  void noRoadsShowsNoHolder() throws Exception {
    final LongestRoadPanel panel = createPanel();

    updateLongestRoad();
    flushSwingEvents();

    assertNull(game.getLongestRoadHolder());
    assertNull(viewModel.getHolderName());
    assertEquals(0, viewModel.getRoadLength());

    assertLongestRoadStatus(panel, "None", "-", "0 VP");
  }

  @Test
  void fiveRoadsUpdatesGameAndGui() throws Exception {
    final LongestRoadPanel panel = createPanel();

    addRoads(alice, 0, 5);

    updateLongestRoad();
    flushSwingEvents();

    assertSame(alice, game.getLongestRoadHolder());
    assertTrue(alice.hasLongestRoad());
    assertEquals(2, alice.getVictoryPoints());

    assertEquals("Alice", viewModel.getHolderName());
    assertEquals(5, viewModel.getRoadLength());

    assertLongestRoadStatus(panel, "Alice", "5", "+2 VP");
  }

  @Test
  void longerRoadTransfersBonusAndUpdatesGui()
          throws Exception {

    final LongestRoadPanel panel = createPanel();

    addRoads(alice, 0, 5);

    updateLongestRoad();
    flushSwingEvents();

    assertSame(alice, game.getLongestRoadHolder());

    addRoads(bob, 8, 6);

    updateLongestRoad();
    flushSwingEvents();

    assertSame(bob, game.getLongestRoadHolder());

    assertFalse(alice.hasLongestRoad());
    assertTrue(bob.hasLongestRoad());

    assertEquals(0, alice.getVictoryPoints());
    assertEquals(2, bob.getVictoryPoints());

    assertEquals("Bob", viewModel.getHolderName());
    assertEquals(6, viewModel.getRoadLength());

    assertLongestRoadStatus(panel, "Bob", "6", "+2 VP");
  }
}
