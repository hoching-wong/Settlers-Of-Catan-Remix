package catan.interfaceadapter.controller;

import catan.usecase.startgame.StartGameInputBoundary;
import catan.usecase.startgame.StartGameInputData;
import java.util.List;
import java.util.Objects;

public class StartGameController {
    private final StartGameInputBoundary startGame;

    public StartGameController(StartGameInputBoundary startGame) {
        this.startGame = Objects.requireNonNull(startGame, "startGame");
    }

    /** Called when the Start Game button is pressed. */
    public void startGame(List<String> playerNames, String cityName) {
        startGame.startGame(new StartGameInputData(playerNames, cityName));
    }
}
