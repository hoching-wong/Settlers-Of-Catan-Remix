package catan.usecase.endturn;

/** Implemented by the presenter. */
public interface EndTurnOutputBoundary {

    /** The turn passed to the next player. */
    void presentTurnEnded(EndTurnOutputData outputData);

    /** The turn could not be ended. Nothing changed. */
    void presentError(String message);
}
