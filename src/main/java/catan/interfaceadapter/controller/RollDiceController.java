package catan.interfaceadapter.controller;

import catan.usecase.rolldice.RollDiceInputBoundary;
import catan.usecase.rolldice.RollDiceInputData;
import java.util.Objects;

public class RollDiceController {
    private final RollDiceInputBoundary rollDice;

    public RollDiceController(RollDiceInputBoundary rollDice) {
        this.rollDice = Objects.requireNonNull(rollDice, "rollDice");
    }

    /** Called when the Roll Dice button is pressed. */
    public void rollDice() {
        rollDice.rollDice(new RollDiceInputData());
    }
}
