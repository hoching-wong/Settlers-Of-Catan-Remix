package catan.usecase.recordwin;

import java.util.Objects;

/**
 * @param winnerName the winner's name, never blank
 */
public record RecordWinInputData(String winnerName) {

    public RecordWinInputData {
        Objects.requireNonNull(winnerName, "winnerName");
        if (winnerName.isBlank()) {
            throw new IllegalArgumentException("The winner name must not be blank");
        }
    }
}
