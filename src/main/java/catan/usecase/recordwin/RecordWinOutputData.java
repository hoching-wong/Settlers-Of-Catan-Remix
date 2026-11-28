package catan.usecase.recordwin;

/**
 * The result of recording a win.
 *
 * @param playerName the name as stored (the spelling it was first recorded with)
 * @param totalWins the player's total wins after this one
 * @param corruptedRecordsSetAside true if unreadable stored records were set aside first
 */
public record RecordWinOutputData(
    String playerName, int totalWins, boolean corruptedRecordsSetAside) {}
