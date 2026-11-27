package catan.usecase.port;

/** The stored win records exist but cannot be understood, for example a half-written file. */
public class CorruptedWinRecordsException extends WinRecordStorageException {

    public CorruptedWinRecordsException(String message) {
        super(message);
    }

    public CorruptedWinRecordsException(String message, Throwable cause) {
        super(message, cause);
    }
}
