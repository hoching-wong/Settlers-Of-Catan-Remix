package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import catan.usecase.restartgame.GameResult;
import catan.usecase.restartgame.PlayerScore;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameOverPresenterTest {
    private final GameOverViewModel gameOver = new GameOverViewModel();
    private final ViewManagerModel viewManager = new ViewManagerModel();
    private final GameOverPresenter presenter = new GameOverPresenter(gameOver, viewManager);

    @Test
    void showsTheWinnerAndEveryScoreOnTheVictoryScreen() {
        presenter.presentGameOver(new GameResult("Bob", List.of(
                new PlayerScore("Ann", 6), new PlayerScore("Bob", 10), new PlayerScore("Cat", 3))));

        assertEquals("Bob", gameOver.getWinnerName());
        assertEquals(List.of(new ScoreEntry("Ann", 6, false), new ScoreEntry("Bob", 10, true),
                new ScoreEntry("Cat", 3, false)), gameOver.getScores());
        assertEquals(Screen.GAME_OVER, viewManager.getActiveScreen());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> presenter.presentGameOver(null));
        assertThrows(NullPointerException.class, () -> new GameOverPresenter(null, viewManager));
        assertThrows(NullPointerException.class, () -> new GameOverPresenter(gameOver, null));
    }
}
