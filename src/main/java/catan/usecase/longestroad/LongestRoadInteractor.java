package catan.usecase.longestroad;

import catan.entity.Game;
import catan.entity.LongestRoadCalculator;
import catan.entity.Player;
import catan.usecase.port.GameRepository;
import java.util.Objects;

/**
 * Handles the Longest Road calculation for the current game.
 */
public class LongestRoadInteractor implements LongestRoadInputBoundary {

  private final GameRepository gameRepository;
  private final LongestRoadOutputBoundary outputBoundary;

  /**
   * Creates a Longest Road interactor.
   *
   * @param gameRepository the repository storing the current game
   * @param outputBoundary the output boundary
   */
  public LongestRoadInteractor(
          GameRepository gameRepository,
          LongestRoadOutputBoundary outputBoundary) {

    this.gameRepository = Objects.requireNonNull(
            gameRepository, "gameRepository");

    this.outputBoundary = Objects.requireNonNull(
            outputBoundary, "outputBoundary");
  }

  /**
   * Recalculates the Longest Road holder and presents the result.
   *
   * @param inputData the input data
   */
  @Override
  public void execute(LongestRoadInputData inputData) {
    Objects.requireNonNull(inputData, "inputData");

    Game game = gameRepository.get();

    if (game == null) {
      outputBoundary.present(new LongestRoadOutputData(null, 0));
      return;
    }

    Player holder = game.updateLongestRoadHolder();
    gameRepository.save(game);

    String holderName = null;
    int roadLength = 0;

    if (holder != null) {
      holderName = holder.getName();

      roadLength = LongestRoadCalculator.longestRoadOf(
              holder, game.getBoard());
    }

    LongestRoadOutputData outputData =
            new LongestRoadOutputData(holderName, roadLength);

    outputBoundary.present(outputData);
  }
}
