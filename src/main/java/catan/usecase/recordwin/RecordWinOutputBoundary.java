package catan.usecase.recordwin;

/** Receives the result of recording a win. */
public interface RecordWinOutputBoundary {

  /**
   * Reports that the win was saved.
   *
   * @param outputData the winner's record after this win
   */
  void presentWinRecorded(RecordWinOutputData outputData);

  /**
   * Reports that the win could not be recorded. Meant for the log, not for the player.
   *
   * @param message what went wrong
   */
  void presentError(String message);
}
