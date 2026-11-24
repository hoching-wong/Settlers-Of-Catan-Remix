package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.restartgame.GameResult;
import catan.usecase.restartgame.PlayerScore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Shows the victory screen. Any presenter whose use case can end the game, such as build,
 * calls {@link #presentGameOver(GameResult)} with the result.
 */
public class GameOverPresenter {
    private final GameOverViewModel gameOver;
    private final ViewManagerModel viewManager;

    public GameOverPresenter(GameOverViewModel gameOver, ViewManagerModel viewManager) {
        this.gameOver = Objects.requireNonNull(gameOver, "gameOver");
        this.viewManager = Objects.requireNonNull(viewManager, "viewManager");
    }

    public void presentGameOver(GameResult result) {
        Objects.requireNonNull(result, "result");
        List<ScoreEntry> entries = new ArrayList<>();
        for (PlayerScore score : result.scores()) {
            entries.add(new ScoreEntry(score.playerName(), score.points(),
                    score.playerName().equals(result.winnerName())));
        }
        gameOver.setResult(result.winnerName(), entries);
        viewManager.setActiveScreen(ViewManagerModel.Screen.GAME_OVER);
    }
}
