package catan.usecase.restartgame;

/** Implemented by the presenter. */
public interface RestartGameOutputBoundary {

    /** The finished game was cleared. Show the start menu. */
    void presentReturnedToMenu(RestartGameOutputData outputData);

    /** There was no finished game to leave. Nothing changed. */
    void presentError(String message);
}
