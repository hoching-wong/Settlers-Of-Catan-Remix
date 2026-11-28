package catan.usecase.leaderboard;

/** Receives the result of loading the leaderboard. */
public interface LeaderboardOutputBoundary {

  /**
   * Reports the leaderboard.
   *
   * @param outputData the rows to show, possibly none
   */
  void presentLeaderboard(LeaderboardOutputData outputData);

  /**
   * Reports that the leaderboard could not be loaded.
   *
   * @param userMessage a short message for the player
   * @param detail what went wrong, for the log
   */
  void presentError(String userMessage, String detail);
}
