package catan.usecase.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import catan.interfaceadapter.controller.LongestRoadController;
import catan.interfaceadapter.presenter.LongestRoadPresenter;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.framework.ui.swing.LongestRoadPanel;
import java.awt.Component;
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
          new LongestRoadInteractor(game, presenter);

  private final LongestRoadController controller =
          new LongestRoadController(interactor);

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
              i, vertices.get(i), vertices.get(i + 1)));
    }

    for (int i = 8; i < 14; i++) {
      edges.add(new Edge(
              i, vertices.get(i), vertices.get(i + 1)));
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
   * Checks whether the panel contains an exact label.
   *
   * @param panel the panel to inspect
   * @param text the expected label
   * @return true if the label exists
   */
  private boolean containsLabel(LongestRoadPanel panel, String text) {
    for (Component component : panel.getComponents()) {
      if (component instanceof JLabel label
              && text.equals(label.getText())) {
        return true;
      }
    }

    return false;
  }

  @Test
  void noRoadsShowsNoHolder() throws Exception {
    final LongestRoadPanel panel = createPanel();

    controller.execute();
    flushSwingEvents();

    assertNull(game.getLongestRoadHolder());
    assertNull(viewModel.getHolderName());
    assertEquals(0, viewModel.getRoadLength());

    assertTrue(containsLabel(
            panel, "Current Holder: None"));

    assertTrue(containsLabel(
            panel, "Victory Point Bonus: 0 VP"));
  }

  @Test
  void fiveRoadsUpdatesGameAndGui() throws Exception {
    final LongestRoadPanel panel = createPanel();

    addRoads(alice, 0, 5);

    controller.execute();
    flushSwingEvents();

    assertSame(alice, game.getLongestRoadHolder());
    assertTrue(alice.hasLongestRoad());
    assertEquals(2, alice.getVictoryPoints());

    assertEquals("Alice", viewModel.getHolderName());
    assertEquals(5, viewModel.getRoadLength());

    assertTrue(containsLabel(
            panel, "Current Holder: Alice"));

    assertTrue(containsLabel(
            panel, "Longest Road Length: 5"));

    assertTrue(containsLabel(
            panel, "Victory Point Bonus: +2 VP"));
  }

  @Test
  void longerRoadTransfersBonusAndUpdatesGui()
          throws Exception {

    final LongestRoadPanel panel = createPanel();

    addRoads(alice, 0, 5);

    controller.execute();
    flushSwingEvents();

    assertSame(alice, game.getLongestRoadHolder());

    addRoads(bob, 8, 6);

    controller.execute();
    flushSwingEvents();

    assertSame(bob, game.getLongestRoadHolder());

    assertFalse(alice.hasLongestRoad());
    assertTrue(bob.hasLongestRoad());

    assertEquals(0, alice.getVictoryPoints());
    assertEquals(2, bob.getVictoryPoints());

    assertEquals("Bob", viewModel.getHolderName());
    assertEquals(6, viewModel.getRoadLength());

    assertTrue(containsLabel(
            panel, "Current Holder: Bob"));

    assertTrue(containsLabel(
            panel, "Longest Road Length: 6"));

    assertTrue(containsLabel(
            panel, "Victory Point Bonus: +2 VP"));
  }
}

