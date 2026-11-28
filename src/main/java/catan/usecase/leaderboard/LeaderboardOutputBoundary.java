package catan.usecase.leaderboard;

public interface LeaderboardOutputBoundary {

    void presentLeaderboard(LeaderboardOutputData outputData);

    /**
     * @param userMessage a short message for the player
     * @param detail what went wrong, for the log
     */
    void presentError(String userMessage, String detail);
}
