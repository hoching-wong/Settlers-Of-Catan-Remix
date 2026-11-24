package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.CityLocation;
import catan.framework.persistence.InMemoryGameRepository;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.presenter.StartGamePresenter;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherUnavailableException;
import catan.usecase.startgame.StartGameInteractor;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** The start menu wired to the real use case, as the application will wire it. */
class StartMenuFlowTest {
    private final InMemoryGameRepository repository = new InMemoryGameRepository();
    private final StartMenuViewModel startMenu =
            new StartMenuViewModel(CityLocation.defaults().stream().map(CityLocation::getName).toList());
    private final GameViewModel game = new GameViewModel();
    private final ViewManagerModel viewManager = new ViewManagerModel();
    private boolean weatherDown;

    private final WeatherDataAccessInterface weather = city -> {
        if (weatherDown) {
            throw new WeatherUnavailableException("offline");
        }
        return 0; // sunny
    };

    private final StartMenuView view = new StartMenuView(startMenu,
            new StartGameController(new StartGameInteractor(repository, weather,
                    new StartGamePresenter(startMenu, game, viewManager), new Random(1))),
            Runnable::run);

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName()) && type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container inner) {
                T found = find(inner, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void flush() {
        try {
            SwingUtilities.invokeAndWait(() -> { });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    private void fill(String city, String... names) {
        find(view, "playerCount", JComboBox.class).setSelectedItem(names.length);
        for (int i = 0; i < names.length; i++) {
            find(view, "name" + (i + 1), JTextField.class).setText(names[i]);
        }
        find(view, "city", JComboBox.class).setSelectedItem(city);
    }

    private void clickStart() {
        find(view, "start", JButton.class).doClick();
        flush();
    }

    private String error() {
        return find(view, "startMenuError", JLabel.class).getText();
    }

    @Test
    void aValidFormStartsTheGameAndMovesToTheGameScreen() {
        fill("Toronto", "Ann", "Bob", "Cat");
        clickStart();

        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertNotNull(repository.get());
        assertEquals(3, repository.get().getPlayers().size());
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
        assertEquals("Weather in Toronto: Sunny", startMenu.getWeatherText());
        assertEquals("Bonus: Lumber x2", startMenu.getBonusText());
        assertEquals(" ", error());
    }

    @Test
    void noCityShowsTheUseCasesMessageAndStaysOnTheMenu() {
        fill(null, "Ann", "Bob");
        clickStart();

        assertEquals("Please select a city.", error());
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
        assertNull(repository.get());
    }

    @Test
    void anEmptyNameShowsWhichPlayerNeedsOne() {
        fill("Toronto", "Ann", "");
        clickStart();
        assertEquals("Player 2 needs a name.", error());
        assertEquals(Screen.START_MENU, viewManager.getActiveScreen());
    }

    @Test
    void duplicateNamesAreRefused() {
        fill("Toronto", "Ann", "ann");
        clickStart();
        assertTrue(error().startsWith("Player names must be different"));
        assertNull(repository.get());
    }

    @Test
    void theErrorGoesAwayOnceTheFormIsFixedAndStartedAgain() {
        fill(null, "Ann", "Bob");
        clickStart();
        assertEquals("Please select a city.", error());

        find(view, "city", JComboBox.class).setSelectedItem("London");
        clickStart();
        assertEquals(" ", error());
        assertEquals(Screen.GAME, viewManager.getActiveScreen());
    }

    @Test
    void aWeatherFailureStillStartsTheGame() {
        weatherDown = true;
        fill("Tokyo", "Ann", "Bob");
        clickStart();

        assertEquals(Screen.GAME, viewManager.getActiveScreen());
        assertEquals("Weather unavailable: normal resource production", startMenu.getWeatherText());
        assertEquals("Bonus: none", startMenu.getBonusText());
    }

    @Test
    void afterANewGameIsRequestedTheMenuIsEmptyAgain() {
        fill("Toronto", "Ann", "Bob");
        clickStart();
        assertEquals(Screen.GAME, viewManager.getActiveScreen());

        startMenu.reset(); // what the restart presenter does when the player leaves the victory screen
        flush();

        assertEquals("", find(view, "name1", JTextField.class).getText());
        assertNull(find(view, "city", JComboBox.class).getSelectedItem());
        assertEquals(List.of(), startMenu.getPlayerNames());
    }
}
