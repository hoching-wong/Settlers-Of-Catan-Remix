package catan.usecase.recordwin;

import catan.usecase.port.CorruptedWinRecordsException;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordRepository;
import catan.usecase.port.WinRecordStorageException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Adds one win to the winner's stored record. Never throws storage failures to its caller. */
public class RecordWinInteractor implements RecordWinInputBoundary {

    private final WinRecordRepository repository;
    private final RecordWinOutputBoundary outputBoundary;

    public RecordWinInteractor(WinRecordRepository repository, RecordWinOutputBoundary outputBoundary) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void recordWin(RecordWinInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");
        boolean setAside = false;
        List<WinRecord> records;
        try {
            try {
                records = new ArrayList<>(repository.load());
            } catch (CorruptedWinRecordsException e) {
                repository.discardCorrupted();
                setAside = true;
                records = new ArrayList<>();
            }
            WinRecord updated = addWin(records, inputData.winnerName());
            repository.save(records);
            outputBoundary.presentWinRecorded(new RecordWinOutputData(updated.name(), updated.wins(), setAside));
        } catch (WinRecordStorageException e) {
            outputBoundary.presentError("Could not record the win: " + e.getMessage());
        }
    }

    private static WinRecord addWin(List<WinRecord> records, String winnerName) {
        String key = WinRecord.keyOf(winnerName);
        for (int i = 0; i < records.size(); i++) {
            WinRecord r = records.get(i);
            if (r.key().equals(key)) {
                WinRecord updated = new WinRecord(r.name(), r.wins() + 1);
                records.set(i, updated);
                return updated;
            }
        }
        WinRecord created = new WinRecord(winnerName, 1);
        records.add(created);
        return created;
    }
}
