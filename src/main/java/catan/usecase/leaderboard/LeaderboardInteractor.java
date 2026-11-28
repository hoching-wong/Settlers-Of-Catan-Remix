package catan.usecase.leaderboard;

import catan.usecase.leaderboard.LeaderboardOutputData.Row;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordRepository;
import catan.usecase.port.WinRecordStorageException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Ranks the stored win records. Only reads: it never changes or sets aside the stored data. */
public class LeaderboardInteractor implements LeaderboardInputBoundary {

    static final int MAX_ROWS = 5;
    private static final String USER_MESSAGE = "The win records could not be loaded.";

    private final WinRecordRepository repository;
    private final LeaderboardOutputBoundary outputBoundary;

    public LeaderboardInteractor(WinRecordRepository repository, LeaderboardOutputBoundary outputBoundary) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void showLeaderboard(LeaderboardInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");
        List<WinRecord> records;
        try {
            records = new ArrayList<>(repository.load());
        } catch (WinRecordStorageException e) {
            outputBoundary.presentError(USER_MESSAGE, e.toString());
            return;
        }
        records.sort(Comparator.comparingInt(WinRecord::wins).reversed().thenComparing(WinRecord::key));

        List<Row> rows = new ArrayList<>();
        int rank = 1;
        for (int i = 0; i < records.size() && rows.size() < MAX_ROWS; i++) {
            WinRecord record = records.get(i);
            if (i > 0 && record.wins() != records.get(i - 1).wins()) {
                rank = i + 1;
            }
            rows.add(new Row(rank, record.name(), record.wins()));
        }
        outputBoundary.presentLeaderboard(new LeaderboardOutputData(rows));
    }
}
