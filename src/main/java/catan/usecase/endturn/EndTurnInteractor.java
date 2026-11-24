package catan.usecase.endturn;

import catan.entity.Game;
import catan.entity.GamePhase;
import catan.usecase.port.GameRepository;
import java.util.Objects;

public class EndTurnInteractor implements EndTurnInputBoundary {
    private final GameRepository gameRepository;
    private final EndTurnOutputBoundary outputBoundary;

    public EndTurnInteractor(GameRepository gameRepository, EndTurnOutputBoundary outputBoundary) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void endTurn(EndTurnInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

        Game game = gameRepository.get();
        if (game == null) {
            outputBoundary.presentError("No game is in progress.");
            return;
        }
        if (game.getPhase() == GamePhase.GAME_OVER) {
            outputBoundary.presentError("The game is over.");
            return;
        }
        if (game.getPhase() == GamePhase.SETUP) {
            outputBoundary.presentError("Finish placing the starting settlements and roads first.");
            return;
        }
        if (!game.hasRolled()) {
            outputBoundary.presentError("Roll the dice before ending your turn.");
            return;
        }

        String previous = game.getCurrentPlayer().getName();
        game.endTurn();
        outputBoundary.presentTurnEnded(new EndTurnOutputData(previous, game.getCurrentPlayer().getName()));
    }
}
