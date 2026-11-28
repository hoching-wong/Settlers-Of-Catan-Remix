package catan.interfaceadapter.viewmodel;

import java.util.List;
import java.util.Objects;

/**
 * What the leaderboard dialog shows: either rows (possibly none) or an error message. The view
 * decides what to say when there are no rows and no error.
 */
public class LeaderboardViewModel extends ViewModel {

  public static final String LEADERBOARD = "leaderboard";

  private List<LeaderboardRow> rows = List.of();
  private String errorMessage = "";

  public List<LeaderboardRow> getRows() {
    return rows;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  /** Shows these rows and clears any error. */
  public void showRows(List<LeaderboardRow> newRows) {
    this.rows = List.copyOf(newRows);
    this.errorMessage = "";
    firePropertyChanged(LEADERBOARD, this);
  }

  /** Shows this error and clears any rows. */
  public void showError(String message) {
    this.errorMessage = Objects.requireNonNull(message, "message");
    this.rows = List.of();
    firePropertyChanged(LEADERBOARD, this);
  }
}
