package catan.usecase.longestroad;

/** Defines the input boundary for the Longest Road use case. */
public interface LongestRoadInputBoundary {

  /**
   * Recalculates the current Longest Road holder.
   *
   * @param inputData the input data for the calculation
   */
  void execute(LongestRoadInputData inputData);
}
