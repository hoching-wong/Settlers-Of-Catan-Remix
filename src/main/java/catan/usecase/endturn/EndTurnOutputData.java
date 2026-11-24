package catan.usecase.endturn;

/**
 * @param previousPlayerName the player who just ended their turn
 * @param nextPlayerName the player whose turn it is now
 */
public record EndTurnOutputData(String previousPlayerName, String nextPlayerName) {
}
