package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import catan.usecase.restartgame.RestartGameOutputData;
import java.util.List;
import org.junit.jupiter.api.Test;

class RestartGamePresenterTest {
    private final StartMenuViewModel startMenu = new StartMenuViewModel(List.of("Toronto"));
    private final GameViewModel game = new GameViewModel();
    private final GameOverViewModel gameOver = new GameOverViewModel();
    private final ViewManagerModel viewManager = new ViewManagerModel();
    private final RestartGamePresenter presenter =
            new RestartGamePresenter(startMenu, game, gameOver, viewManager);

    private void aFinishedGameIsOnScreen() {
        startMenu.setGameStarted(List.of("Ann", "Bob"), "Toronto", "w", "b");
        game.startGame(List.of("Ann", "Bob"), "Ann", "Roll the dice");
        game.setMessage("note");
        gameOver.setResult("Ann", List.of(new ScoreEntry("Ann", 10, true), new ScoreEntry("Bob", 4, false)));
        viewManager.setActiveScreen(Screen.GAME_OVER);
    }

    @Test
    void returningClearsTheOldGameAndShowsTheStartMenu() {
        aFinishedGameIsOnScreen();
        presenter.presentReturnedToMenu(new RestartGameOutputData());

        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
        assertTrue(startMenu.getPlayerNames().isEmpty());
        assertEquals("", startMenu.getCityName());
        assertTrue(game.getPlayerNames().isEmpty());
        assertEquals("", game.getCurrentPlayerName());
        assertEquals("", game.getMessage());
        assertEquals("", gameOver.getWinnerName());
        assertTrue(gameOver.getScores().isEmpty());
        assertEquals(List.of("Toronto"), startMenu.getCityNames());
    }

    @Test
    void anErrorIsShownOnTheVictoryScreen() {
        aFinishedGameIsOnScreen();
        presenter.presentError("The game is not over yet.");
        assertEquals("The game is not over yet.", gameOver.getErrorMessage());
        assertEquals(Screen.GAME_OVER, viewManager.getActiveScreen());
        assertEquals("Ann", gameOver.getWinnerName());
    }

    @Test
    void rejectsNullViewModels() {
        assertThrows(NullPointerException.class,
                () -> new RestartGamePresenter(null, game, gameOver, viewManager));
        assertThrows(NullPointerException.class,
                () -> new RestartGamePresenter(startMenu, null, gameOver, viewManager));
        assertThrows(NullPointerException.class,
                () -> new RestartGamePresenter(startMenu, game, null, viewManager));
        assertThrows(NullPointerException.class,
                () -> new RestartGamePresenter(startMenu, game, gameOver, null));
    }
}
