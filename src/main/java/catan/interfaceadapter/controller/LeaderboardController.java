package catan.interfaceadapter.controller;

import catan.usecase.leaderboard.LeaderboardInputBoundary;
import catan.usecase.leaderboard.LeaderboardInputData;
import java.util.Objects;

/** Asks for the leaderboard to be loaded. */
public class LeaderboardController {

  private final LeaderboardInputBoundary interactor;

  /**
   * Creates the controller.
   *
   * @param interactor the use case that loads the leaderboard
   */
  public LeaderboardController(LeaderboardInputBoundary interactor) {
    this.interactor = Objects.requireNonNull(interactor);
  }

  /** Asks for the leaderboard to be loaded into its view model. */
  public void show() {
    interactor.showLeaderboard(new LeaderboardInputData());
  }
}
