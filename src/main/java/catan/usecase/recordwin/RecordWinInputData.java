package catan.usecase.recordwin;

import java.util.Objects;

/**
 * The winner of a finished game.
 *
 * @param winnerName the winner's name, never blank
 */
public record RecordWinInputData(String winnerName) {

  /** Checks that the name is present and not blank. */
  public RecordWinInputData {
    Objects.requireNonNull(winnerName, "winnerName");
    if (winnerName.isBlank()) {
      throw new IllegalArgumentException("The winner name must not be blank");
    }
  }
}
