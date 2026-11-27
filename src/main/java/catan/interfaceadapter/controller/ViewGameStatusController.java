
package catan.interfaceadapter.controller;

import catan.usecase.viewgamestatus.ViewGameStatusInputBoundary;
import java.util.Objects;

/**
 * Controller for requesting the current game status.
 */
public class ViewGameStatusController {
  private final ViewGameStatusInputBoundary interactor;

  /**
   * Creates a controller with the given input boundary.
   *
   * @param interactor the input boundary for viewing game status
   */
  public ViewGameStatusController(ViewGameStatusInputBoundary interactor) {
    this.interactor = Objects.requireNonNull(interactor);
  }

  /**
   * Requests the latest game status.
   */
  public void execute() {
    interactor.viewGameStatus();
  }
}

