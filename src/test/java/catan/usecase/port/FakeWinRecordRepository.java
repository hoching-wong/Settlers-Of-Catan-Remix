package catan.usecase.port;

import java.util.ArrayList;
import java.util.List;

/** A shared in-memory fake of {@link WinRecordRepository} for use case tests. */
public class FakeWinRecordRepository implements WinRecordRepository {

    public List<WinRecord> stored = new ArrayList<>();
    public WinRecordStorageException loadFailure;
    public WinRecordStorageException saveFailure;
    public WinRecordStorageException discardFailure;
    /** While true, load() reports corrupted data. discardCorrupted() clears it and empties storage. */
    public boolean corrupted;
    public final List<String> calls = new ArrayList<>();

    @Override
    public List<WinRecord> load() throws WinRecordStorageException {
        calls.add("load");
        if (loadFailure != null) {
            throw loadFailure;
        }
        if (corrupted) {
            throw new CorruptedWinRecordsException("corrupted");
        }
        return new ArrayList<>(stored);
    }

    @Override
    public void save(List<WinRecord> records) throws WinRecordStorageException {
        calls.add("save");
        if (saveFailure != null) {
            throw saveFailure;
        }
        stored = new ArrayList<>(records);
    }

    @Override
    public void discardCorrupted() throws WinRecordStorageException {
        calls.add("discard");
        if (discardFailure != null) {
            throw discardFailure;
        }
        corrupted = false;
        stored = new ArrayList<>();
    }
}
