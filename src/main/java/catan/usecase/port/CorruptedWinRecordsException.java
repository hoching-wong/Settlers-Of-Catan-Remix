package catan.usecase.port;

/** The stored win records exist but cannot be understood, for example a half-written file. */
public class CorruptedWinRecordsException extends WinRecordStorageException {

  /**
   * Creates the exception.
   *
   * @param message what is wrong with the data
   */
  public CorruptedWinRecordsException(String message) {
    super(message);
  }

  /**
   * Creates the exception.
   *
   * @param message what is wrong with the data
   * @param cause the underlying failure
   */
  public CorruptedWinRecordsException(String message, Throwable cause) {
    super(message, cause);
  }
}
