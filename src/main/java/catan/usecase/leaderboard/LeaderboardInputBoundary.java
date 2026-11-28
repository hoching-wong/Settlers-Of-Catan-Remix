package catan.usecase.leaderboard;

/** Shows the players with the most wins. */
public interface LeaderboardInputBoundary {

  /**
   * Loads the leaderboard and reports it through the output boundary.
   *
   * @param inputData the request, which carries nothing
   */
  void showLeaderboard(LeaderboardInputData inputData);
}
