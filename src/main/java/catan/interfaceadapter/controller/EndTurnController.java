package catan.interfaceadapter.controller;

import catan.usecase.endturn.EndTurnInputBoundary;
import catan.usecase.endturn.EndTurnInputData;
import java.util.Objects;

public class EndTurnController {
    private final EndTurnInputBoundary endTurn;

    public EndTurnController(EndTurnInputBoundary endTurn) {
        this.endTurn = Objects.requireNonNull(endTurn, "endTurn");
    }

    /** Called when the End Turn button is pressed. */
    public void endTurn() {
        endTurn.endTurn(new EndTurnInputData());
    }
}
