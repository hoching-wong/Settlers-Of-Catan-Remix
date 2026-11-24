package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.usecase.startgame.StartGameOutputBoundary;
import catan.usecase.startgame.StartGameOutputData;
import java.util.Objects;

public class StartGamePresenter implements StartGameOutputBoundary {
    private final StartMenuViewModel startMenu;
    private final GameViewModel game;
    private final ViewManagerModel viewManager;
    private final LongestRoadViewModel longestRoadViewModel;

    public StartGamePresenter(StartMenuViewModel startMenu,
                              GameViewModel game,
                              ViewManagerModel viewManager,
                              LongestRoadViewModel longestRoadViewModel) {
        this.startMenu = Objects.requireNonNull(startMenu, "startMenu");
        this.game = Objects.requireNonNull(game, "game");
        this.viewManager = Objects.requireNonNull(viewManager, "viewManager");
        this.longestRoadViewModel = Objects.requireNonNull(
                longestRoadViewModel, "longestRoadViewModel");
    }

    @Override
    public void presentGameStarted(StartGameOutputData out) {
        String weatherText = weatherText(out);
        String bonusText = bonusText(out);
        startMenu.setGameStarted(out.playerNames(), out.cityName(), weatherText, bonusText);
        game.startGame(out.playerNames(), out.playerNames().get(0), Text.PLACE_SETTLEMENT);
        game.setWeather(weatherText, bonusText);

        longestRoadViewModel.updateState(null, 0);

        viewManager.setActiveScreen(ViewManagerModel.Screen.GAME);
    }

    @Override
    public void presentError(String message) {
        startMenu.setError(message);
    }

    private static String weatherText(StartGameOutputData out) {
        if (!out.weatherAvailable()) {
            return "Weather unavailable: normal resource production";
        }
        if (out.condition() == null) {
            return "Weather in " + out.cityName() + ": no category, normal resource production";
        }
        return "Weather in " + out.cityName() + ": " + Text.title(out.condition());
    }

    private static String bonusText(StartGameOutputData out) {
        if (!out.hasBonus()) {
            return "Bonus: none";
        }
        return "Bonus: " + Text.title(out.bonusResource()) + " x" + out.multiplier();
    }
}