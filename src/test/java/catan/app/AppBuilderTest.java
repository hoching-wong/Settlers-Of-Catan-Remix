package catan.app;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.City;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.Vertex;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.usecase.weather.WeatherGateway;
import catan.usecase.weather.WeatherUnavailableException;
import catan.usecase.restartgame.GameResult;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** The whole application, built the way Main builds it, driven through its Swing components. */
class AppBuilderTest {
    private final InMemoryGameRepository repository = new InMemoryGameRepository();
    private final WeatherGateway sunny = city -> 0;
    private final AppBuilder builder = new AppBuilder(new Random(5), sunny, repository, Runnable::run);
    private final ScreenSwitcher app = builder.buildScreens();

    // ---- helpers ----

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

    private String label(String name) {
        return find(app, name, JLabel.class).getText();
    }

    private void click(String name) {
        find(app, name, JButton.class).doClick();
        flush();
    }

    /** Visible all the way up to the switcher (the window itself is never shown in tests). */
    private boolean isVisibleChain(Component c) {
        for (Component x = c; x != null && x != app; x = x.getParent()) {
            if (!x.isVisible()) {
                return false;
            }
        }
        return true;
    }

    private void startAGame(String city, String... names) {
        find(app, "playerCount", JComboBox.class).setSelectedItem(names.length);
        for (int i = 0; i < names.length; i++) {
            find(app, "name" + (i + 1), JTextField.class).setText(names[i]);
        }
        find(app, "city", JComboBox.class).setSelectedItem(city);
        click("start");
    }

    private void finishSetup(Game game) {
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex spot = game.getBoard().getVertices().stream()
                    .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
                    .findFirst().orElseThrow();
            builder.getPlaceInitialController().place(spot.getId());
            builder.getPlaceInitialController().place(game.getLastPlacedSettlement().getAdjacentEdges().stream()
                    .filter(e -> !e.isOccupied()).findFirst().orElseThrow().getId());
        }
        flush();
    }

    // ---- the application ----

    @Test
    void opensOnTheStartMenu() {
        assertTrue(isVisibleChain(find(app, "start", JButton.class)));
        assertFalse(isVisibleChain(find(app, "endTurn", JButton.class)));
        assertFalse(isVisibleChain(find(app, "backToMenu", JButton.class)));
    }

    @Test
    void aValidStartShowsTheGameScreenWithTheTurn() {
        startAGame("Toronto", "Ann", "Bob");

        assertFalse(isVisibleChain(find(app, "start", JButton.class)));
        assertTrue(isVisibleChain(find(app, "endTurn", JButton.class)));
        assertEquals("Current turn: Ann", label("turn"));
        assertEquals("Place a settlement", label("instruction"));
        assertEquals("Players: Ann, Bob", label("players"));
        assertNotNull(repository.get());
    }

    @Test
    void aMissingCityKeepsTheStartMenuAndShowsTheMessage() {
        startAGame(null, "Ann", "Bob");
        assertTrue(isVisibleChain(find(app, "start", JButton.class)));
        assertEquals("Please select a city.", find(app, "startMenuError", JLabel.class).getText().strip());
        assertNull(repository.get());
    }

    @Test
    void theWeatherIsShownOnTheGameScreen() {
        startAGame("Toronto", "Ann", "Bob");
        assertEquals("Weather in Toronto: Sunny   |   Bonus: Lumber x2", label("weather"));
    }

    @Test
    void withoutAWeatherServiceTheGameStillStarts() {
        AppBuilder offline = new AppBuilder(new Random(5), city -> {
            throw new WeatherUnavailableException("offline");
        }, new InMemoryGameRepository(), Runnable::run);
        ScreenSwitcher offlineApp = offline.buildScreens();
        ((JComboBox<?>) find(offlineApp, "city", JComboBox.class)).setSelectedItem("Tokyo");
        find(offlineApp, "name1", JTextField.class).setText("Ann");
        find(offlineApp, "name2", JTextField.class).setText("Bob");
        find(offlineApp, "start", JButton.class).doClick();
        flush();
        assertEquals("Weather unavailable: normal resource production   |   Bonus: none",
                find(offlineApp, "weather", JLabel.class).getText());
    }

    @Test
    void theDefaultBuilderStartsGamesWithoutBonusesUntilTheRealGatewayIsPlugged() {
        // the production constructor uses the stand-in gateway and a background thread
        AppBuilder production = new AppBuilder();
        assertNotNull(production.buildScreens());
        assertNotNull(production.getGameView());
        assertNotNull(production.getPlaceInitialController());
        assertNotNull(production.getGameOverPresenter());
    }

    @Test
    void thePartsForOtherPeopleAreOnlyAvailableOnceBuilt() {
        AppBuilder fresh = new AppBuilder(new Random(1), sunny, new InMemoryGameRepository(), Runnable::run);
        assertThrows(IllegalStateException.class, fresh::getGameView);
        assertThrows(IllegalStateException.class, fresh::getPlaceInitialController);
        assertThrows(IllegalStateException.class, fresh::getGameOverPresenter);
    }

    @Test
    void endTurnBeforeRollingShowsTheRefusalOnTheGameScreen() {
        startAGame("Toronto", "Ann", "Bob");
        finishSetup(repository.get());
        assertEquals("Roll the dice", label("instruction"));

        click("endTurn");
        assertEquals("Roll the dice before ending your turn.", label("gameError"));
        assertEquals("Current turn: Ann", label("turn"));
    }

    @Test
    void endTurnAfterRollingPassesTheTurn() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        finishSetup(game);

        game.markRolled(); // the roll-dice use case will do this
        click("endTurn");
        assertEquals("Current turn: Bob", label("turn"));
        assertEquals("Ann ended their turn.", label("message"));
    }

    @Test
    void thePlayThroughFromMenuToVictoryAndBackToTheMenu() {
        startAGame("London", "Ann", "Bob");
        Game game = repository.get();
        finishSetup(game);

        Player winner = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            winner.addBuilding(new City(winner, new Vertex(4000 + i)));
        }
        assertTrue(game.checkVictory());
        builder.getGameOverPresenter().presentGameOver(GameResult.from(game));
        flush();

        assertTrue(isVisibleChain(find(app, "backToMenu", JButton.class)));
        assertEquals("Winner: Ann", label("winner"));
        assertEquals("Ann: 10 points  (winner)", label("score0"));
        assertEquals("Bob: 2 points", label("score1"));

        click("backToMenu");
        assertTrue(isVisibleChain(find(app, "start", JButton.class)));
        assertNull(repository.get());
        assertEquals("", find(app, "name1", JTextField.class).getText());

        startAGame("Tokyo", "Cat", "Dan", "Eve");
        assertEquals("Current turn: Cat", label("turn"));
        assertEquals(3, repository.get().getPlayers().size());
    }

    @Test
    void constructorRejectsNulls() {
        assertThrows(NullPointerException.class,
                () -> new AppBuilder(null, sunny, repository, Runnable::run));
        assertThrows(NullPointerException.class,
                () -> new AppBuilder(new Random(), null, repository, Runnable::run));
        assertThrows(NullPointerException.class,
                () -> new AppBuilder(new Random(), sunny, null, Runnable::run));
        assertThrows(NullPointerException.class,
                () -> new AppBuilder(new Random(), sunny, repository, null));
    }
}
