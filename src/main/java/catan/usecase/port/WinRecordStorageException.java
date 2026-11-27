package catan.usecase.port;

/** The win records could not be read or written. Thrown by a {@link WinRecordRepository}. */
public class WinRecordStorageException extends Exception {

    public WinRecordStorageException(String message) {
        super(message);
    }

    public WinRecordStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
