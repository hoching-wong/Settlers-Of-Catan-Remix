package catan.usecase.rolldice;

/** Implemented by the presenter. */
public interface RollDiceOutputBoundary {

    /** The dice were rolled and the resources were given out. */
    void presentRolled(RollDiceOutputData outputData);

    /** The dice could not be rolled. Nothing changed. */
    void presentError(String message);
}
