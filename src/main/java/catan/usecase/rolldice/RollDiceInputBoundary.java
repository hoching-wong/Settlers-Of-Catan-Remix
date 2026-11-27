package catan.usecase.rolldice;

/** The current player rolls the dice and collects the resources it produces. */
public interface RollDiceInputBoundary {

    void rollDice(RollDiceInputData inputData);
}
