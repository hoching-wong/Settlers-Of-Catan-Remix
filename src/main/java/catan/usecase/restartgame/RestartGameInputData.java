package catan.usecase.restartgame;

import java.util.Objects;

/**
 * Which button was pressed on the victory screen.
 *
 * @param choice NEW_GAME or EXIT. Both leave the finished game and return to the start menu.
 */
public record RestartGameInputData(Choice choice) {

    public enum Choice {
        NEW_GAME,
        EXIT
    }

    public RestartGameInputData {
        Objects.requireNonNull(choice, "choice");
    }
}
