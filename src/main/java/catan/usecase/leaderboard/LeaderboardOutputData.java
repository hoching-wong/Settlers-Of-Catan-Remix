package catan.usecase.leaderboard;

import java.util.List;

/**
 * @param rows the rows to show, best first. Empty when nothing has been recorded yet.
 */
public record LeaderboardOutputData(List<Row> rows) {

    public LeaderboardOutputData {
        rows = List.copyOf(rows);
    }

    /**
     * @param rank 1 for the most wins; players with the same wins share a rank (1, 2, 2, 4)
     */
    public record Row(int rank, String name, int wins) {}
}
