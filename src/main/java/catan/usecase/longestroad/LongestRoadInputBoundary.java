package catan.usecase.longestroad;

/** Input boundary for recalculating the Longest Road. */
public interface LongestRoadInputBoundary {

  /**
   * Executes the Longest Road use case.
   *
   * @param inputData the input data
   * @return the updated Longest Road information
   */
  LongestRoadOutputData execute(LongestRoadInputData inputData);
}
