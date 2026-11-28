package catan.usecase.port;

/** The win records could not be read or written. Thrown by a {@link WinRecordRepository}. */
public class WinRecordStorageException extends Exception {

  /**
   * Creates the exception.
   *
   * @param message what went wrong
   */
  public WinRecordStorageException(String message) {
    super(message);
  }

  /**
   * Creates the exception.
   *
   * @param message what went wrong
   * @param cause the underlying failure
   */
  public WinRecordStorageException(String message, Throwable cause) {
    super(message, cause);
  }
}
