package catan.usecase.longestroad;

import catan.entity.Game;
import catan.entity.LongestRoadCalculator;
import catan.entity.Player;
import java.util.Objects;

/**
 * Handles the Longest Road calculation for the current game.
 */
public class LongestRoadInteractor implements LongestRoadInputBoundary {

  /**
   * Recalculates the Longest Road holder and returns the result.
   *
   * @param inputData the input data
   * @return the updated Longest Road information
   */
  @Override
  public LongestRoadOutputData execute(LongestRoadInputData inputData) {
    Objects.requireNonNull(inputData, "inputData");

    Game game = inputData.game();

    if (game == null) {
      return new LongestRoadOutputData(null, 0);
    }

    Player holder = game.updateLongestRoadHolder();

    String holderName = null;
    int roadLength = 0;

    if (holder != null) {
      holderName = holder.getName();

      roadLength = LongestRoadCalculator.longestRoadOf(
              holder, game.getBoard());
    }

    LongestRoadOutputData outputData =
            new LongestRoadOutputData(holderName, roadLength);

    return outputData;
  }
}
