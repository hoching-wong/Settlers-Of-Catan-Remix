package catan.interfaceadapter.viewmodel;

/** One row of the leaderboard as the view shows it. */
public record LeaderboardRow(int rank, String name, int wins) {}
