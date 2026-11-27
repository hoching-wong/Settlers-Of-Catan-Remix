package catan.usecase.longestroad;

/** Defines how Longest Road results are presented. */
public interface LongestRoadOutputBoundary {

  /**
   * Presents the current Longest Road information.
   *
   * @param outputData the result to display
   */
  void present(LongestRoadOutputData outputData);
}
