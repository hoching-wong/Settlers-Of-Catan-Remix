package catan.usecase.viewgamestatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.Settlement;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.entity.WeatherType;
import catan.framework.persistence.InMemoryGameRepository;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests the ViewGameStatusInteractor. */
class ViewGameStatusInteractorTest {

  /** Records the output produced by the interactor. */
  private static class TestPresenter implements ViewGameStatusOutputBoundary {
    private ViewGameStatusOutputData outputData;
    private String errorMessage;

    @Override
    public void prepareSuccessView(ViewGameStatusOutputData outputData) {
      this.outputData = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }

  /** Tests that an error is returned when no game exists. */
  @Test
  void noGameInProgress() {
    InMemoryGameRepository repository = new InMemoryGameRepository();
    TestPresenter presenter = new TestPresenter();

    ViewGameStatusInteractor interactor = new ViewGameStatusInteractor(repository, presenter);

    interactor.viewGameStatus();

    assertEquals("No game is in progress.", presenter.errorMessage);
    assertNull(presenter.outputData);
  }

  /** Tests that the current game status is retrieved correctly. */
  @Test
  void retrievesCurrentGameStatus() {
    final InMemoryGameRepository repository = new InMemoryGameRepository();
    final TestPresenter presenter = new TestPresenter();

    Player alice = new Player("Alice");
    Player bob = new Player("Bob");

    alice.getResources().addResource(ResourceType.LUMBER, 3);
    alice.getResources().addResource(ResourceType.BRICK, 2);

    WeatherData weather = WeatherData.fromWmoCode(0);

    Game game =
            new Game(
                    BoardFactory.createStandardBoard(new Random(1)),
                    List.of(alice, bob),
                    weather,
                    new Dice(new Random(1)));

    game.startGame();
    repository.save(game);

    ViewGameStatusInteractor interactor = new ViewGameStatusInteractor(repository, presenter);

    interactor.viewGameStatus();

    ViewGameStatusOutputData result = presenter.outputData;

    assertNotNull(result);
    assertNull(presenter.errorMessage);
    assertEquals(2, result.players().size());
    assertEquals("Alice", result.currentPlayerName());

    ViewGameStatusOutputData.PlayerStatus aliceStatus = result.players().get(0);

    assertEquals("Alice", aliceStatus.playerName());
    assertEquals(0, aliceStatus.victoryPoints());
    assertEquals(3, aliceStatus.resources().get(ResourceType.LUMBER));
    assertEquals(2, aliceStatus.resources().get(ResourceType.BRICK));
    assertEquals(0, aliceStatus.resources().get(ResourceType.ORE));

    assertEquals(WeatherType.SUNNY, result.weatherCondition());
    assertEquals(ResourceType.LUMBER, result.bonusResource());
    assertEquals(2, result.multiplier());
  }

  /** Tests player resources and victory points without a weather bonus. */
  @Test
  void retrievesDifferentPlayerStatusWithoutWeatherBonus() {
    InMemoryGameRepository repository = new InMemoryGameRepository();
    TestPresenter presenter = new TestPresenter();

    Player alice = new Player("Alice");
    Player bob = new Player("Bob");

    alice.getResources().addResource(ResourceType.LUMBER, 4);
    bob.getResources().addResource(ResourceType.ORE, 3);
    bob.getResources().addResource(ResourceType.WOOL, 2);

    Board board = BoardFactory.createStandardBoard(new Random(1));

    Vertex vertex = board.getVertices().get(0);
    board.placeBuilding(vertex, new Settlement(alice, vertex));

    Game game =
            new Game(
                    board,
                    List.of(alice, bob),
                    WeatherData.none(),
                    new Dice(new Random(1)));

    game.startGame();
    repository.save(game);

    ViewGameStatusInteractor interactor =
            new ViewGameStatusInteractor(repository, presenter);

    interactor.viewGameStatus();

    ViewGameStatusOutputData result = presenter.outputData;

    assertNotNull(result);
    assertNull(presenter.errorMessage);
    assertEquals(2, result.players().size());

    ViewGameStatusOutputData.PlayerStatus aliceStatus =
            result.players().get(0);

    ViewGameStatusOutputData.PlayerStatus bobStatus =
            result.players().get(1);

    assertEquals("Alice", aliceStatus.playerName());
    assertEquals(1, aliceStatus.victoryPoints());
    assertEquals(4, aliceStatus.resources().get(ResourceType.LUMBER));
    assertEquals(0, aliceStatus.resources().get(ResourceType.ORE));
    assertEquals(0, aliceStatus.resources().get(ResourceType.WOOL));

    assertEquals("Bob", bobStatus.playerName());
    assertEquals(0, bobStatus.victoryPoints());
    assertEquals(0, bobStatus.resources().get(ResourceType.LUMBER));
    assertEquals(3, bobStatus.resources().get(ResourceType.ORE));
    assertEquals(2, bobStatus.resources().get(ResourceType.WOOL));

    assertEquals(ResourceType.values().length, aliceStatus.resources().size());
    assertEquals(ResourceType.values().length, bobStatus.resources().size());

    assertNull(result.weatherCondition());
    assertNull(result.bonusResource());
    assertEquals(1, result.multiplier());
  }

  /** Tests that retrieving the status again reflects changes in the game. */
  @Test
  void retrievesUpdatedGameStatus() {
    InMemoryGameRepository repository = new InMemoryGameRepository();
    TestPresenter presenter = new TestPresenter();

    Player alice = new Player("Alice");
    Player bob = new Player("Bob");

    Board board = BoardFactory.createStandardBoard(new Random(1));

    Game game =
            new Game(
                    board,
                    List.of(alice, bob),
                    WeatherData.none(),
                    new Dice(new Random(1)));

    game.startGame();
    repository.save(game);

    ViewGameStatusInteractor interactor =
            new ViewGameStatusInteractor(repository, presenter);

    interactor.viewGameStatus();

    ViewGameStatusOutputData initialResult = presenter.outputData;

    assertNotNull(initialResult);
    assertEquals("Alice", initialResult.currentPlayerName());
    assertEquals(0, initialResult.players().get(0).victoryPoints());
    assertEquals(
            0, initialResult.players().get(0).resources().get(ResourceType.GRAIN));

    Vertex vertex = board.getVertices().get(0);
    board.placeBuilding(vertex, new Settlement(alice, vertex));

    game.setLastPlacedSettlement(vertex);
    game.advanceSetup();

    Edge edge = vertex.getAdjacentEdges().get(0);
    board.placeRoad(edge, new Road(alice, edge));

    game.advanceSetup();

    alice.getResources().addResource(ResourceType.GRAIN, 3);

    interactor.viewGameStatus();

    ViewGameStatusOutputData updatedResult = presenter.outputData;

    assertNotNull(updatedResult);
    assertNull(presenter.errorMessage);
    assertEquals("Bob", updatedResult.currentPlayerName());
    assertEquals(1, updatedResult.players().get(0).victoryPoints());
    assertEquals(
            3, updatedResult.players().get(0).resources().get(ResourceType.GRAIN));

    assertEquals("Alice", initialResult.currentPlayerName());
    assertEquals(0, initialResult.players().get(0).victoryPoints());
    assertEquals(
            0, initialResult.players().get(0).resources().get(ResourceType.GRAIN));
  }
}
