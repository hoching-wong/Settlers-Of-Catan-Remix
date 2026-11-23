package catan.usecase.restartgame;

import catan.entity.Game;
import catan.entity.GamePhase;
import catan.usecase.port.GameRepository;
import java.util.Objects;

public class RestartGameInteractor implements RestartGameInputBoundary {
    private final GameRepository gameRepository;
    private final RestartGameOutputBoundary outputBoundary;

    public RestartGameInteractor(GameRepository gameRepository, RestartGameOutputBoundary outputBoundary) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void restartGame(RestartGameInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

        Game game = gameRepository.get();
        if (game == null) {
            outputBoundary.presentError("No game is in progress.");
            return;
        }
        if (game.getPhase() != GamePhase.GAME_OVER) {
            outputBoundary.presentError("The game is not over yet.");
            return;
        }

        gameRepository.clear();
        outputBoundary.presentReturnedToMenu(new RestartGameOutputData(inputData.choice()));
    }
}
