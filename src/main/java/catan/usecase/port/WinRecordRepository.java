package catan.usecase.port;

import java.util.List;

/** Where the win records are kept between runs of the game. Implemented in the framework layer. */
public interface WinRecordRepository {

    /**
     * Reads every record. Returns an empty list if nothing has been recorded yet.
     *
     * @throws CorruptedWinRecordsException if the stored data exists but cannot be understood
     * @throws WinRecordStorageException if the data cannot be read for any other reason
     */
    List<WinRecord> load() throws WinRecordStorageException;

    /**
     * Replaces the stored records with these. If it fails, the previously stored records are
     * left as they were.
     *
     * @throws WinRecordStorageException if the records cannot be written
     */
    void save(List<WinRecord> records) throws WinRecordStorageException;

    /**
     * Sets the unreadable stored data aside so that it is not lost, and leaves the storage
     * empty. Call it after {@link #load()} reports corrupted data.
     *
     * @throws WinRecordStorageException if the data cannot be set aside
     */
    void discardCorrupted() throws WinRecordStorageException;
}
