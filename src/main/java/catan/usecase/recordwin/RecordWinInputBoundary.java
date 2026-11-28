package catan.usecase.recordwin;

/** Records one more win for the winner of a finished game. */
public interface RecordWinInputBoundary {

  /**
   * Adds one win to the winner and saves it.
   *
   * @param inputData the winner
   */
  void recordWin(RecordWinInputData inputData);
}
