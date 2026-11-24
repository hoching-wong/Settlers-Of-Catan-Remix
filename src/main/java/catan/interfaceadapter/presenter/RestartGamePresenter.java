package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.restartgame.RestartGameOutputBoundary;
import catan.usecase.restartgame.RestartGameOutputData;
import java.util.Objects;

public class RestartGamePresenter implements RestartGameOutputBoundary {
    private final StartMenuViewModel startMenu;
    private final GameViewModel game;
    private final GameOverViewModel gameOver;
    private final ViewManagerModel viewManager;

    public RestartGamePresenter(StartMenuViewModel startMenu, GameViewModel game, GameOverViewModel gameOver,
                                ViewManagerModel viewManager) {
        this.startMenu = Objects.requireNonNull(startMenu, "startMenu");
        this.game = Objects.requireNonNull(game, "game");
        this.gameOver = Objects.requireNonNull(gameOver, "gameOver");
        this.viewManager = Objects.requireNonNull(viewManager, "viewManager");
    }

    @Override
    public void presentReturnedToMenu(RestartGameOutputData outputData) {
        game.reset();
        gameOver.reset();
        startMenu.reset();
        viewManager.setActiveScreen(ViewManagerModel.Screen.START_MENU);
    }

    @Override
    public void presentError(String message) {
        gameOver.setError(message);
    }
}
