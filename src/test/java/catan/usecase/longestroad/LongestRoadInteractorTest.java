package catan.usecase.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.Board;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.Road;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests for LongestRoadInteractor. */
class LongestRoadInteractorTest {

  /** Captures the output produced by the interactor. */
  private static class TestOutputBoundary implements LongestRoadOutputBoundary {

    private LongestRoadOutputData lastOutput;
    private int callCount;

    @Override
    public void present(LongestRoadOutputData outputData) {
      lastOutput = outputData;
      callCount++;
    }
  }

  /** Creates an isolated game for each test. */
  private static class TestGame {

    private final Board board;
    private final Player alice = new Player("Alice");
    private final Player bob = new Player("Bob");
    private final Game game;

    private TestGame() {
      List<Vertex> vertices = new ArrayList<>();
      List<Edge> edges = new ArrayList<>();

      for (int i = 0; i <= 20; i++) {
        vertices.add(new Vertex(i));
      }

      for (int i = 0; i < 10; i++) {
        edges.add(new Edge(i, vertices.get(i), vertices.get(i + 1)));
      }

      for (int i = 11; i < 20; i++) {
        edges.add(new Edge(i, vertices.get(i), vertices.get(i + 1)));
      }

      board = new Board(List.of(), vertices, edges);

      game = new Game(board, List.of(alice, bob), WeatherData.none(), new Dice(new Random(1)));
    }

    private void addRoads(Player player, int start, int count) {
      for (int i = start; i < start + count; i++) {
        Edge edge = board.getEdge(i);
        board.placeRoad(edge, new Road(player, edge));
      }
    }
  }

  @Test
  void noRoadsPresentsNoHolder() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    interactor.execute(new LongestRoadInputData());

    assertNull(output.lastOutput.holderName());
    assertEquals(0, output.lastOutput.roadLength());
    assertNull(data.game.getLongestRoadHolder());
    assertEquals(1, output.callCount);
  }

  @Test
  void fiveRoadsEarnLongestRoad() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    data.addRoads(data.alice, 0, 5);

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    interactor.execute(new LongestRoadInputData());

    assertEquals("Alice", output.lastOutput.holderName());
    assertEquals(5, output.lastOutput.roadLength());
    assertSame(data.alice, data.game.getLongestRoadHolder());
    assertTrue(data.alice.hasLongestRoad());
    assertEquals(2, data.alice.getVictoryPoints());
  }

  @Test
  void fewerThanFiveRoadsDoNotQualify() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    data.addRoads(data.alice, 0, 4);

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    interactor.execute(new LongestRoadInputData());

    assertNull(output.lastOutput.holderName());
    assertEquals(0, output.lastOutput.roadLength());
    assertFalse(data.alice.hasLongestRoad());
  }

  @Test
  void longerRoadTransfersHolderAndBonus() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    data.addRoads(data.alice, 0, 5);
    interactor.execute(new LongestRoadInputData());

    data.addRoads(data.bob, 11, 6);
    interactor.execute(new LongestRoadInputData());

    assertEquals("Bob", output.lastOutput.holderName());
    assertEquals(6, output.lastOutput.roadLength());
    assertSame(data.bob, data.game.getLongestRoadHolder());
    assertFalse(data.alice.hasLongestRoad());
    assertTrue(data.bob.hasLongestRoad());
    assertEquals(0, data.alice.getVictoryPoints());
    assertEquals(2, data.bob.getVictoryPoints());
    assertEquals(2, output.callCount);
  }

  @Test
  void tieKeepsCurrentHolder() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    data.addRoads(data.alice, 0, 5);
    interactor.execute(new LongestRoadInputData());

    data.addRoads(data.bob, 11, 5);
    interactor.execute(new LongestRoadInputData());

    assertSame(data.alice, data.game.getLongestRoadHolder());
    assertEquals("Alice", output.lastOutput.holderName());
    assertEquals(5, output.lastOutput.roadLength());
    assertTrue(data.alice.hasLongestRoad());
    assertFalse(data.bob.hasLongestRoad());
  }

  @Test
  void rejectsNullDependencies() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    assertThrows(NullPointerException.class, () -> new LongestRoadInteractor(null, output));

    assertThrows(NullPointerException.class, () -> new LongestRoadInteractor(data.game, null));
  }

  @Test
  void rejectsNullInputData() {
    TestGame data = new TestGame();
    TestOutputBoundary output = new TestOutputBoundary();

    LongestRoadInteractor interactor = new LongestRoadInteractor(data.game, output);

    assertThrows(NullPointerException.class, () -> interactor.execute(null));
  }
}
