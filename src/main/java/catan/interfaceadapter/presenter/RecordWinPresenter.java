package catan.interfaceadapter.presenter;

import catan.usecase.recordwin.RecordWinOutputBoundary;
import catan.usecase.recordwin.RecordWinOutputData;
import java.util.Objects;

/** Reports whether a win was recorded to the log only. Nothing is shown on screen. */
public class RecordWinPresenter implements RecordWinOutputBoundary {

    private final System.Logger logger;

    public RecordWinPresenter(System.Logger logger) {
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void presentWinRecorded(RecordWinOutputData outputData) {
        if (outputData.corruptedRecordsSetAside()) {
            logger.log(System.Logger.Level.WARNING,
                    "The stored win records were unreadable and were set aside; starting a new file.");
        }
        logger.log(System.Logger.Level.INFO,
                "Recorded a win for " + outputData.playerName() + " (total " + outputData.totalWins() + ").");
    }

    @Override
    public void presentError(String message) {
        logger.log(System.Logger.Level.ERROR, message);
    }
}
