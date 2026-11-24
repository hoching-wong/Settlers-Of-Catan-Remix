package catan.usecase.startgame;

/** Implemented by the presenter. */
public interface StartGameOutputBoundary {

    /** A new game was created and saved. */
    void presentGameStarted(StartGameOutputData outputData);

    /** The input was not valid. No game was created. */
    void presentError(String message);
}
