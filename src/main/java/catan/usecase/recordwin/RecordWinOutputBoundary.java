package catan.usecase.recordwin;

public interface RecordWinOutputBoundary {

    void presentWinRecorded(RecordWinOutputData outputData);

    /** The win could not be recorded. Meant for the log, not for the player. */
    void presentError(String message);
}
