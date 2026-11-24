package catan.interfaceadapter.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What the victory screen shows: the winner and everyone's final score. */
public class GameOverViewModel extends ViewModel {
    public static final String RESULT = "result";
    public static final String ERROR = "error";

    private String winnerName = "";
    private List<ScoreEntry> scores = List.of();
    private String errorMessage = "";

    public String getWinnerName() {
        return winnerName;
    }

    /** The final scores in turn order. */
    public List<ScoreEntry> getScores() {
        return scores;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setResult(String winnerName, List<ScoreEntry> scores) {
        this.winnerName = winnerName;
        this.scores = Collections.unmodifiableList(new ArrayList<>(scores));
        this.errorMessage = "";
        firePropertyChanged(RESULT, winnerName);
    }

    public void setError(String message) {
        this.errorMessage = message == null ? "" : message;
        firePropertyChanged(ERROR, errorMessage);
    }

    public void reset() {
        winnerName = "";
        scores = List.of();
        errorMessage = "";
        firePropertyChanged(RESULT, winnerName);
        firePropertyChanged(ERROR, errorMessage);
    }
}
