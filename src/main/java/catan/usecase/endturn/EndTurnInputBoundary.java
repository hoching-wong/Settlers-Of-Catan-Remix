package catan.usecase.endturn;

/** The current player ends their turn and play passes to the next player. */
public interface EndTurnInputBoundary {

    void endTurn(EndTurnInputData inputData);
}
