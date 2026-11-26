package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.endturn.EndTurnOutputBoundary;
import catan.usecase.endturn.EndTurnOutputData;
import java.util.Objects;

public class EndTurnPresenter implements EndTurnOutputBoundary {
    private final GameViewModel game;
    private final Runnable refreshGameStatus;

    public EndTurnPresenter(GameViewModel game) {
        this(game, () -> {});
    }

    public EndTurnPresenter(GameViewModel game,
                            Runnable refreshGameStatus) {
        this.game = Objects.requireNonNull(game, "game");
        this.refreshGameStatus =
                Objects.requireNonNull(refreshGameStatus, "refreshGameStatus");
    }

    @Override
    public void presentTurnEnded(EndTurnOutputData out) {
        game.setTurn(out.nextPlayerName(), Text.ROLL_DICE);
        game.setMessage(out.previousPlayerName() + " ended their turn.");
        refreshGameStatus.run();
    }

    @Override
    public void presentError(String message) {
        game.setError(message);
    }
}
