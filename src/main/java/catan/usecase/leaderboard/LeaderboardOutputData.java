package catan.usecase.leaderboard;

import java.util.List;

/**
 * The rows of the leaderboard.
 *
 * @param rows the rows to show, best first. Empty when nothing has been recorded yet.
 */
public record LeaderboardOutputData(List<Row> rows) {

  /** Keeps an unmodifiable copy of the rows. */
  public LeaderboardOutputData {
    rows = List.copyOf(rows);
  }

  /**
   * One row of the leaderboard.
   *
   * @param rank 1 for the most wins; players with the same wins share a rank (1, 2, 2, 4)
   * @param name the player's name
   * @param wins the player's total wins
   */
  public record Row(int rank, String name, int wins) {}
}
