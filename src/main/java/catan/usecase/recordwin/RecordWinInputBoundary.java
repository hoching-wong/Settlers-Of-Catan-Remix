package catan.usecase.recordwin;

/** Records one more win for the winner of a finished game. */
public interface RecordWinInputBoundary {

    void recordWin(RecordWinInputData inputData);
}
