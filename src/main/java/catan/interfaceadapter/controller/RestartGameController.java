package catan.interfaceadapter.controller;

import catan.usecase.restartgame.RestartGameInputBoundary;
import catan.usecase.restartgame.RestartGameInputData;
import java.util.Objects;

public class RestartGameController {
    private final RestartGameInputBoundary restartGame;

    public RestartGameController(RestartGameInputBoundary restartGame) {
        this.restartGame = Objects.requireNonNull(restartGame, "restartGame");
    }

    /** Called when the Back to Main Menu button on the victory screen is pressed. */
    public void returnToMenu() {
        restartGame.restartGame(new RestartGameInputData());
    }
}
