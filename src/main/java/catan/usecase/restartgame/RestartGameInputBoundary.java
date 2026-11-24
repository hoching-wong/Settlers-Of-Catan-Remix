package catan.usecase.restartgame;

/** Leaves a finished game for the start menu: UC7 "restart a new game or exit to the main menu". */
public interface RestartGameInputBoundary {

    void restartGame(RestartGameInputData inputData);
}
