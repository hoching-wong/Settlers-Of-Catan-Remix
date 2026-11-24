package catan.interfaceadapter.controller;

import catan.usecase.longestroad.LongestRoadInputBoundary;
import catan.usecase.longestroad.LongestRoadInputData;
import java.util.Objects;

/** Controller for recalculating the Longest Road. */
public class LongestRoadController {

  private final LongestRoadInputBoundary inputBoundary;

  /**
   * Creates a Longest Road controller.
   *
   * @param inputBoundary the Longest Road input boundary
   */
  public LongestRoadController(LongestRoadInputBoundary inputBoundary) {
    this.inputBoundary = Objects.requireNonNull(inputBoundary, "inputBoundary");
  }

  /** Requests recalculation of the Longest Road holder. */
  public void execute() {
    LongestRoadInputData inputData = new LongestRoadInputData();
    inputBoundary.execute(inputData);
  }
}
