package catan.usecase.longestroad;

import catan.entity.Game;
import catan.entity.LongestRoadCalculator;
import catan.entity.Player;
import java.util.Objects;

/** Recalculates the Longest Road holder and prepares the result. */
public class LongestRoadInteractor implements LongestRoadInputBoundary {

  private final Game game;
  private final LongestRoadOutputBoundary outputBoundary;

  /**
   * Creates the Longest Road interactor.
   *
   * @param game the current game
   * @param outputBoundary the output boundary
   */
  public LongestRoadInteractor(Game game, LongestRoadOutputBoundary outputBoundary) {

    this.game = Objects.requireNonNull(game, "game");
    this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
  }

  /**
   * Updates the Longest Road holder and presents the result.
   *
   * @param inputData the request to recalculate Longest Road
   */
  @Override
  public void execute(LongestRoadInputData inputData) {
    Objects.requireNonNull(inputData, "inputData");

    Player holder = game.updateLongestRoadHolder();

    String holderName = null;
    int roadLength = 0;

    if (holder != null) {
      holderName = holder.getName();
      roadLength = LongestRoadCalculator.longestRoadOf(holder, game.getBoard());
    }

    LongestRoadOutputData outputData = new LongestRoadOutputData(holderName, roadLength);

    outputBoundary.present(outputData);
  }
}
