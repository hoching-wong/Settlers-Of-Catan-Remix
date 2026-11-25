package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.ResourceType;
import catan.entity.WeatherType;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import catan.usecase.startgame.StartGameOutputData;
import java.util.List;
import org.junit.jupiter.api.Test;

class StartGamePresenterTest {
    private final StartMenuViewModel startMenu = new StartMenuViewModel(List.of("Toronto"));
    private final GameViewModel game = new GameViewModel();
    private final ViewManagerModel viewManager = new ViewManagerModel();
    private final StartGamePresenter presenter = new StartGamePresenter(startMenu, game, viewManager);

    @Test
    void theFirstClickOnTheBoardPlacesASettlement() {
        BoardViewModel board = new BoardViewModel();
        board.getState().select("ROAD", BoardState.EDGE, java.util.Set.of(3));
        int[] fired = {0};
        board.addPropertyChangeListener(e -> fired[0]++);
        new StartGamePresenter(startMenu, game, viewManager, board).presentGameStarted(new StartGameOutputData(
                List.of("Ann", "Bob"), "Toronto", true, WeatherType.SUNNY, ResourceType.LUMBER, 2));

        assertEquals(BoardState.VERTEX, board.getState().getSetupTarget());
        assertNull(board.getState().getSelectedStructure(), "no build left over from an earlier game");
        assertEquals(1, fired[0]);
    }

    @Test
    void anErrorLeavesTheBoardAlone() {
        BoardViewModel board = new BoardViewModel();
        new StartGamePresenter(startMenu, game, viewManager, board).presentError("Please select a city.");
        assertNull(board.getState().getSetupTarget());
    }

    @Test
    void rejectsANullBoardViewModel() {
        assertThrows(NullPointerException.class, () -> new StartGamePresenter(startMenu, game, viewManager, null));
    }

    @Test
    void aSunnyStartShowsTheBonusAndMovesToTheGameScreen() {
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "Toronto", true,
                WeatherType.SUNNY, ResourceType.LUMBER, 2));

        assertEquals(List.of("Ann", "Bob"), startMenu.getPlayerNames());
        assertEquals("Toronto", startMenu.getCityName());
        assertEquals("Weather in Toronto: Sunny", startMenu.getWeatherText());
        assertEquals("Bonus: Lumber x2", startMenu.getBonusText());
        assertEquals("", startMenu.getErrorMessage());

        assertEquals(List.of("Ann", "Bob"), game.getPlayerNames());
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertEquals("Weather in Toronto: Sunny", game.getWeatherText());
        assertEquals("Bonus: Lumber x2", game.getBonusText());
    }

    @Test
    void everyBonusIsNamedAfterItsResource() {
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "London", true,
                WeatherType.STORMY, ResourceType.ORE, 2));
        assertEquals("Weather in London: Stormy", startMenu.getWeatherText());
        assertEquals("Bonus: Ore x2", startMenu.getBonusText());
    }

    @Test
    void fogHasNoBonus() {
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "Toronto", true,
                WeatherType.FOG, null, 1));
        assertEquals("Weather in Toronto: Fog", startMenu.getWeatherText());
        assertEquals("Bonus: none", startMenu.getBonusText());
    }

    @Test
    void aCodeWithNoCategoryIsExplained() {
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "Toronto", true,
                null, null, 1));
        assertTrue(startMenu.getWeatherText().contains("normal resource production"));
        assertEquals("Bonus: none", startMenu.getBonusText());
    }

    @Test
    void unavailableWeatherIsExplained() {
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "Toronto", false,
                null, null, 1));
        assertEquals("Weather unavailable: normal resource production", startMenu.getWeatherText());
        assertEquals("Bonus: none", startMenu.getBonusText());
        assertEquals(Screen.GAME, viewManager.getActiveScreen(), "the game still starts");
    }

    @Test
    void anErrorStaysOnTheMenu() {
        presenter.presentError("Please select a city.");
        assertEquals("Please select a city.", startMenu.getErrorMessage());
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
        assertTrue(game.getPlayerNames().isEmpty());
    }

    @Test
    void aSuccessfulStartClearsTheOldError() {
        presenter.presentError("Please select a city.");
        presenter.presentGameStarted(new StartGameOutputData(List.of("Ann", "Bob"), "Toronto", true,
                WeatherType.CLOUDY, ResourceType.BRICK, 2));
        assertEquals("", startMenu.getErrorMessage());
    }

    @Test
    void rejectsNullViewModels() {
        assertThrows(NullPointerException.class, () -> new StartGamePresenter(null, game, viewManager));
        assertThrows(NullPointerException.class, () -> new StartGamePresenter(startMenu, null, viewManager));
        assertThrows(NullPointerException.class, () -> new StartGamePresenter(startMenu, game, null));
    }
}
