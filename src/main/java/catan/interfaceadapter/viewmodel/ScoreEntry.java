package catan.interfaceadapter.viewmodel;

/** One row of the final scores on the victory screen. */
public record ScoreEntry(String playerName, int points, boolean winner) {
}
