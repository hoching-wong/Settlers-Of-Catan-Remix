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
        edges.add(new Edge(
                i, vertices.get(i), vertices.get(i + 1)));
      }

      for (int i = 11; i < 20; i++) {
        edges.add(new Edge(
                i, vertices.get(i), vertices.get(i + 1)));
      }

      board = new Board(List.of(), vertices, edges);

      game = new Game(
              board,
              List.of(alice, bob),
              WeatherData.none(),
              new Dice(new Random(1)));
    }

    private void addRoads(Player player, int start, int count) {
      for (int i = start; i < start + count; i++) {
        Edge edge = board.getEdge(i);
        board.placeRoad(edge, new Road(player, edge));
      }
    }
  }

  @Test
  void noRoadsReturnsNoHolder() {
    TestGame data = new TestGame();

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertNull(outputData.holderName());
    assertEquals(0, outputData.roadLength());
    assertNull(data.game.getLongestRoadHolder());
  }

  @Test
  void fiveRoadsEarnLongestRoad() {
    TestGame data = new TestGame();

    data.addRoads(data.alice, 0, 5);

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertEquals("Alice", outputData.holderName());
    assertEquals(5, outputData.roadLength());
    assertSame(data.alice, data.game.getLongestRoadHolder());
    assertTrue(data.alice.hasLongestRoad());
    assertEquals(2, data.alice.getVictoryPoints());
  }

  @Test
  void fewerThanFiveRoadsDoNotQualify() {
    TestGame data = new TestGame();

    data.addRoads(data.alice, 0, 4);

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertNull(outputData.holderName());
    assertEquals(0, outputData.roadLength());
    assertFalse(data.alice.hasLongestRoad());
  }

  @Test
  void longerRoadTransfersHolderAndBonus() {
    TestGame data = new TestGame();

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    data.addRoads(data.alice, 0, 5);

    LongestRoadOutputData firstOutput =
            interactor.execute(new LongestRoadInputData(data.game));

    assertEquals("Alice", firstOutput.holderName());
    assertEquals(5, firstOutput.roadLength());
    assertEquals(2, data.alice.getVictoryPoints());

    data.addRoads(data.bob, 11, 6);

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertEquals("Bob", outputData.holderName());
    assertEquals(6, outputData.roadLength());
    assertSame(data.bob, data.game.getLongestRoadHolder());
    assertFalse(data.alice.hasLongestRoad());
    assertTrue(data.bob.hasLongestRoad());
    assertEquals(0, data.alice.getVictoryPoints());
    assertEquals(2, data.bob.getVictoryPoints());
  }

  @Test
  void tieKeepsCurrentHolder() {
    TestGame data = new TestGame();

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    data.addRoads(data.alice, 0, 5);
    interactor.execute(new LongestRoadInputData(data.game));

    data.addRoads(data.bob, 11, 5);

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertSame(data.alice, data.game.getLongestRoadHolder());
    assertEquals("Alice", outputData.holderName());
    assertEquals(5, outputData.roadLength());
    assertTrue(data.alice.hasLongestRoad());
    assertFalse(data.bob.hasLongestRoad());
  }

  @Test
  void repeatedExecutionDoesNotDuplicateBonus() {
    TestGame data = new TestGame();

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    data.addRoads(data.alice, 0, 5);

    interactor.execute(new LongestRoadInputData(data.game));

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(data.game));

    assertSame(data.alice, data.game.getLongestRoadHolder());
    assertEquals("Alice", outputData.holderName());
    assertEquals(5, outputData.roadLength());
    assertTrue(data.alice.hasLongestRoad());
    assertEquals(2, data.alice.getVictoryPoints());
  }

  @Test
  void rejectsNullInputData() {
    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    assertThrows(
            NullPointerException.class,
            () -> interactor.execute(null));
  }

  @Test
  void noCurrentGameReturnsNoHolder() {
    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    LongestRoadOutputData outputData =
            interactor.execute(new LongestRoadInputData(null));

    assertNull(outputData.holderName());
    assertEquals(0, outputData.roadLength());
  }

  @Test
  void usesNewGameWhenInputGameChanges() {
    TestGame first = new TestGame();
    TestGame second = new TestGame();

    LongestRoadInteractor interactor =
            new LongestRoadInteractor();

    first.addRoads(first.alice, 0, 5);

    LongestRoadOutputData firstOutput =
            interactor.execute(new LongestRoadInputData(first.game));

    assertEquals("Alice", firstOutput.holderName());

    second.addRoads(second.bob, 11, 6);

    LongestRoadOutputData secondOutput =
            interactor.execute(new LongestRoadInputData(second.game));

    assertEquals("Bob", secondOutput.holderName());
    assertEquals(6, secondOutput.roadLength());
    assertSame(second.bob, second.game.getLongestRoadHolder());
  }
}
