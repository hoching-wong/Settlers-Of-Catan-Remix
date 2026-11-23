package catan.interfaceadapter.controller;

import catan.usecase.restartgame.RestartGameInputBoundary;
import catan.usecase.restartgame.RestartGameInputData;
import java.util.Objects;

public class RestartGameController {
    private final RestartGameInputBoundary restartGame;

    public RestartGameController(RestartGameInputBoundary restartGame) {
        this.restartGame = Objects.requireNonNull(restartGame, "restartGame");
    }

    /** Called when the New Game button on the victory screen is pressed. */
    public void newGame() {
        restartGame.restartGame(new RestartGameInputData(RestartGameInputData.Choice.NEW_GAME));
    }

    /** Called when the Exit button on the victory screen is pressed. */
    public void exit() {
        restartGame.restartGame(new RestartGameInputData(RestartGameInputData.Choice.EXIT));
    }
}
