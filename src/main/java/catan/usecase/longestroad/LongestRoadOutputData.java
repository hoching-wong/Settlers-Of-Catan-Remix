package catan.usecase.longestroad;

/**
 * Contains the Longest Road information to display.
 *
 * @param holderName the current holder's name, or null
 * @param roadLength the holder's longest road length
 */
public record LongestRoadOutputData(String holderName, int roadLength) {

  /** Validates the output data. */
  public LongestRoadOutputData {
    if (roadLength < 0) {
      throw new IllegalArgumentException("Road length cannot be negative");
    }

    if (holderName == null && roadLength != 0) {
      throw new IllegalArgumentException("Road length must be zero without a holder");
    }
  }
}
