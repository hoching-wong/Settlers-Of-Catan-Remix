package catan.app;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.City;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Tile;
import catan.entity.Vertex;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.ui.swing.BoardPanel;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.interfaceadapter.viewmodel.BoardPoint;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.TileView;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherUnavailableException;
import catan.usecase.restartgame.GameResult;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** The whole application, built the way Main builds it, driven through its Swing components. */
class AppBuilderTest {
    private final InMemoryGameRepository repository = new InMemoryGameRepository();
    private final WeatherDataAccessInterface sunny = city -> 0;
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

    private static <T extends Component> T findType(Container root, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container inner) {
                T found = findType(inner, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static AbstractButton buttonWithText(Container root, String text) {
        for (Component c : root.getComponents()) {
            if (c instanceof AbstractButton b && text.equals(b.getText())) {
                return b;
            }
            if (c instanceof Container inner) {
                AbstractButton found = buttonWithText(inner, text);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private BoardState board() {
        return builder.getBoardViewModel().getState();
    }

    /** Clicks the board panel at a board position, the way a mouse click would. */
    private void clickBoardAt(BoardPoint p) {
        BoardPanel panel = findType(app, BoardPanel.class);
        panel.setSize(720, 640);
        // the same scale BoardPanel uses to fit the board
        double scale = Math.min(720 / (2 * (Math.sqrt(3) * 2.5 + 0.55)), 640 / (2 * (4.0 + 0.55)));
        int x = (int) Math.round(360 + p.x() * scale);
        int y = (int) Math.round(320 + p.y() * scale);
        panel.dispatchEvent(new MouseEvent(panel, MouseEvent.MOUSE_CLICKED, System.currentTimeMillis(), 0,
                x, y, 1, false, MouseEvent.BUTTON1));
        flush();
    }

    private void clickVertex(int id) {
        clickBoardAt(board().getVertices().get(id).position());
    }

    private void clickEdge(int id) {
        clickBoardAt(board().getEdges().get(id).midpoint());
    }

    private static Vertex firstValidSpot(Game game) {
        Player p = game.getCurrentPlayer();
        return game.getBoard().getVertices().stream()
                .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
                .findFirst().orElseThrow();
    }

    private static Edge freeEdgeNextToLastSettlement(Game game) {
        return game.getLastPlacedSettlement().getAdjacentEdges().stream()
                .filter(e -> !e.isOccupied()).findFirst().orElseThrow();
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
    void theDefaultBuilderCreatesApplicationWithWeatherService() {
        // building the screens does not make a weather API request
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

    // ---- the board ----

    @Test
    void theBoardViewTakesThePlaceOfThePlaceholder() {
        assertNull(find(app, "boardPlaceholder", JLabel.class));
        assertNotNull(findType(app, BoardPanel.class));
        assertNotNull(buttonWithText(app, "Road"));
        assertNotNull(buttonWithText(app, "Settlement"));
        assertNotNull(buttonWithText(app, "City"));
    }

    @Test
    void startingAGameDrawsTheBoardTheGameIsPlayedOn() {
        startAGame("Toronto", "Ann", "Bob");
        Board played = repository.get().getBoard();

        assertEquals(19, board().getTiles().size());
        assertEquals(played.getVertices().size(), board().getVertices().size());
        assertEquals(played.getEdges().size(), board().getEdges().size());
        for (int i = 0; i < 19; i++) {
            Tile tile = played.getTiles().get(i);
            TileView drawn = board().getTiles().get(i);
            assertEquals(tile.getTerrain().name(), drawn.terrain(), "tile " + i);
            assertEquals(tile.getNumberToken(), drawn.numberToken(), "tile " + i);
        }
        assertEquals(BoardState.VERTEX, board().getSetupTarget());
    }

    @Test
    void theBoardHasNoNeighbouringRedNumbers() {
        startAGame("Toronto", "Ann", "Bob");
        for (TileView a : board().getTiles()) {
            for (TileView b : board().getTiles()) {
                boolean neighbours = a.id() != b.id() && Math.hypot(a.centre().x() - b.centre().x(),
                        a.centre().y() - b.centre().y()) < 2.0;
                assertFalse(neighbours && a.red() && b.red(), "tiles " + a.id() + " and " + b.id());
            }
        }
    }

    @Test
    void everyGameGetsANewBoard() {
        startAGame("Toronto", "Ann", "Bob");
        Board first = repository.get().getBoard();
        repository.clear();
        startAGame("Toronto", "Ann", "Bob");
        assertNotSame(first, repository.get().getBoard());
    }

    @Test
    void clickingTheBoardPlacesTheInitialSettlementAndRoad() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        Vertex spot = firstValidSpot(game);

        clickVertex(spot.getId());
        assertTrue(spot.isOccupied());
        assertEquals("Ann", spot.getBuilding().getOwner().getName());
        assertEquals("SETTLEMENT", board().getVertices().get(spot.getId()).building());
        assertEquals(BoardState.EDGE, board().getSetupTarget());
        assertEquals("Place a road next to your new settlement", label("instruction"));

        Edge road = freeEdgeNextToLastSettlement(game);
        clickEdge(road.getId());
        assertTrue(road.isOccupied());
        assertEquals(0, board().getEdges().get(road.getId()).ownerIndex());
        assertEquals(BoardState.VERTEX, board().getSetupTarget());
        assertEquals("Current turn: Bob", label("turn"));
    }

    @Test
    void anEdgeClickWhileASettlementIsExpectedChangesNothing() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        clickEdge(0);
        assertTrue(game.getBoard().getEdges().stream().noneMatch(Edge::isOccupied));
        assertTrue(game.getBoard().getVertices().stream().noneMatch(Vertex::isOccupied));
        assertEquals("Place a settlement", label("instruction"));
    }

    @Test
    void aRefusedSetupClickShowsTheReason() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        Vertex spot = firstValidSpot(game);
        clickVertex(spot.getId());
        clickEdge(freeEdgeNextToLastSettlement(game).getId());

        clickVertex(spot.getId()); // Bob clicks Ann's settlement
        assertEquals("That spot is already taken.", label("gameError"));
        assertEquals("Current turn: Bob", label("turn"));
    }

    @Test
    void theWholeSetupCanBePlayedByClickingTheBoard() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        while (game.getPhase() == GamePhase.SETUP) {
            clickVertex(firstValidSpot(game).getId());
            clickEdge(freeEdgeNextToLastSettlement(game).getId());
        }
        assertEquals("Roll the dice", label("instruction"));
        assertEquals("Current turn: Ann", label("turn"));
        assertNull(board().getSetupTarget(), "clicks belong to the build menu now");
        long drawnSettlements = board().getVertices().stream().filter(v -> "SETTLEMENT".equals(v.building())).count();
        assertEquals(4, drawnSettlements);
    }

    @Test
    void theBuildMenuAsksForTheDiceFirst() {
        startAGame("Toronto", "Ann", "Bob");
        finishSetup(repository.get());
        buttonWithText(app, "Road").doClick();
        flush();
        assertEquals("Roll the dice before building.", board().getMessage());
        assertTrue(board().isError());
    }

    @Test
    void afterRollingARoadCanBeBuiltFromTheBuildMenu() {
        startAGame("Toronto", "Ann", "Bob");
        Game game = repository.get();
        finishSetup(game);
        game.markRolled(); // the roll-dice use case will do this
        Player ann = game.getCurrentPlayer();
        ann.getResources().addResource(ResourceType.LUMBER, 1);
        ann.getResources().addResource(ResourceType.BRICK, 1);
        int roadsBefore = ann.getRoads().size();

        buttonWithText(app, "Road").doClick();
        flush();
        assertEquals("ROAD", board().getSelectedStructure());
        assertFalse(board().getHighlightedEdgeIds().isEmpty());

        int target = board().getHighlightedEdgeIds().iterator().next();
        clickEdge(target);
        assertEquals(roadsBefore + 1, ann.getRoads().size());
        assertEquals(0, board().getEdges().get(target).ownerIndex());
        assertTrue(board().getMessage().startsWith("Ann built a road"), board().getMessage());
        assertNull(board().getSelectedStructure());
    }

    @Test
    void theBoardViewModelIsOnlyAvailableOnceBuilt() {
        AppBuilder fresh = new AppBuilder(new Random(1), sunny, new InMemoryGameRepository(), Runnable::run);
        assertThrows(IllegalStateException.class, fresh::getBoardViewModel);
        fresh.buildScreens();
        assertNotNull(fresh.getBoardViewModel());
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