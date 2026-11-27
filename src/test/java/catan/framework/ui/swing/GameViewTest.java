package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static catan.framework.ui.swing.SwingTestSupport.flush;
import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.usecase.endturn.EndTurnInputData;
import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

class GameViewTest {
    private final GameViewModel viewModel = new GameViewModel();
    private final List<EndTurnInputData> endTurnClicks = new ArrayList<>();
    private final GameView view = new GameView(viewModel, new EndTurnController(endTurnClicks::add));

    private String text(String name) {
        return find(view, name, JLabel.class).getText();
    }

    @Test
    void startsBlankWithAnEndTurnButtonAndABoardPlaceholder() {
        assertEquals(" ", text("turn"));
        assertEquals(" ", text("instruction"));
        assertEquals(" ", text("players"));
        assertEquals(" ", text("weather"));
        assertEquals(" ", text("message"));
        assertEquals(" ", text("gameError"));
        assertNotNull(find(view, "endTurn", JButton.class));
        assertNotNull(find(view, "boardPlaceholder", JLabel.class));
    }

    @Test
    void showsWhoseTurnItIsAndWhatToDo() {
        viewModel.startGame(List.of("Ann", "Bob", "Cat"), "Ann", "Place a settlement");
        flush();
        assertEquals("Current turn: Ann", text("turn"));
        assertEquals("Place a settlement", text("instruction"));
        assertEquals("Players: Ann, Bob, Cat", text("players"));
    }

    @Test
    void theTurnChangesWhenTheViewModelChanges() {
        viewModel.startGame(List.of("Ann", "Bob"), "Ann", "Roll the dice");
        viewModel.setTurn("Bob", "Roll the dice");
        flush();
        assertEquals("Current turn: Bob", text("turn"));
    }

    @Test
    void showsTheWeatherAndBonus() {
        viewModel.setWeather("Weather in Toronto: Sunny", "Bonus: Lumber x2");
        flush();
        assertEquals("Weather in Toronto: Sunny   |   Bonus: Lumber x2", text("weather"));
    }

    @Test
    void showsMessagesAndErrors() {
        viewModel.setMessage("Bob received 1 Lumber, 2 Grain.");
        viewModel.setError("That spot is already taken.");
        flush();
        assertEquals("Bob received 1 Lumber, 2 Grain.", text("message"));
        assertEquals("That spot is already taken.", text("gameError"));

        viewModel.setError("");
        flush();
        assertEquals(" ", text("gameError"));
    }

    @Test
    void endTurnButtonCallsTheController() {
        find(view, "endTurn", JButton.class).doClick();
        find(view, "endTurn", JButton.class).doClick();
        assertEquals(2, endTurnClicks.size());
    }

    @Test
    void updatesFromABackgroundThreadAreShown() throws InterruptedException {
        Thread worker = new Thread(() -> viewModel.setTurn("Cat", "Roll the dice"));
        worker.start();
        worker.join();
        flush();
        assertEquals("Current turn: Cat", text("turn"));
    }

    @Test
    void resettingClearsTheScreen() {
        viewModel.startGame(List.of("Ann", "Bob"), "Ann", "Roll the dice");
        viewModel.setWeather("w", "b");
        viewModel.setMessage("m");
        viewModel.setError("e");
        viewModel.reset();
        flush();
        for (String name : List.of("turn", "instruction", "players", "weather", "message", "gameError")) {
            assertEquals(" ", text(name), name);
        }
    }

    @Test
    void theBoardViewReplacesThePlaceholder() {
        JPanel board = new JPanel();
        board.setName("fakeBoard");
        view.setBoardComponent(board);

        assertNotNull(find(view, "fakeBoard", JPanel.class));
        assertNull(find(view, "boardPlaceholder", JLabel.class));
        assertThrows(NullPointerException.class, () -> view.setBoardComponent(null));
    }

    @Test
    void constructorRejectsNulls() {
        EndTurnController controller = new EndTurnController(endTurnClicks::add);
        assertThrows(NullPointerException.class, () -> new GameView(null, controller));
        assertThrows(NullPointerException.class, () -> new GameView(viewModel, null));
    }

    @Test
    void longestRoadAppearsAboveGameStatus() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameView gameView = new GameView(
                    viewModel,
                    new EndTurnController(endTurnClicks::add),
                    new LongestRoadViewModel());

            JPanel statusPanel = new JPanel();
            gameView.setGameStatusPanel(statusPanel);

            gameView.setSize(1000, 860);
            gameView.doLayout();

            BorderLayout layout = (BorderLayout) gameView.getLayout();

            JPanel sidePanel = assertInstanceOf(
                    JPanel.class,
                    layout.getLayoutComponent(BorderLayout.EAST));

            sidePanel.doLayout();

            BorderLayout sideLayout = (BorderLayout) sidePanel.getLayout();

            LongestRoadPanel longestRoadPanel = assertInstanceOf(
                    LongestRoadPanel.class,
                    sideLayout.getLayoutComponent(BorderLayout.NORTH));

            assertSame(statusPanel,
                    sideLayout.getLayoutComponent(BorderLayout.CENTER));

            assertSame(sidePanel, longestRoadPanel.getParent());
            assertSame(sidePanel, statusPanel.getParent());

            assertEquals(2, sidePanel.getComponentCount());
            assertEquals(320, longestRoadPanel.getWidth());
            assertEquals(150, longestRoadPanel.getHeight());
            assertEquals(longestRoadPanel.getWidth(), statusPanel.getWidth());

            assertTrue(statusPanel.getHeight() > 0);
            assertTrue(longestRoadPanel.getY() + longestRoadPanel.getHeight()
                    <= statusPanel.getY());
        });
    }

    @Test
    void replacingGameStatusPreservesLongestRoad() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            GameView gameView = new GameView(
                    viewModel,
                    new EndTurnController(endTurnClicks::add),
                    new LongestRoadViewModel());

            JPanel firstStatusPanel = new JPanel();
            JPanel secondStatusPanel = new JPanel();

            gameView.setGameStatusPanel(firstStatusPanel);
            gameView.setGameStatusPanel(secondStatusPanel);

            BorderLayout layout = (BorderLayout) gameView.getLayout();

            JPanel sidePanel = assertInstanceOf(
                    JPanel.class,
                    layout.getLayoutComponent(BorderLayout.EAST));

            BorderLayout sideLayout = (BorderLayout) sidePanel.getLayout();

            assertInstanceOf(
                    LongestRoadPanel.class,
                    sideLayout.getLayoutComponent(BorderLayout.NORTH));

            assertSame(secondStatusPanel,
                    sideLayout.getLayoutComponent(BorderLayout.CENTER));

            assertNull(firstStatusPanel.getParent());
            assertEquals(2, sidePanel.getComponentCount());
        });
    }

    @Test
    void longestRoadIsEmbeddedInsideGameStatus() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            LongestRoadViewModel longestRoadViewModel =
                    new LongestRoadViewModel();

            GameView gameView = new GameView(
                    viewModel,
                    new EndTurnController(endTurnClicks::add),
                    longestRoadViewModel);

            LongestRoadPanel originalPanel =
                    find(gameView, "longest-road-panel", LongestRoadPanel.class);

            assertNotNull(originalPanel);

            GameStatusPanel statusPanel = new GameStatusPanel(
                    new GameStatusViewModel(),
                    new StartMenuViewModel(List.of("Toronto")));

            gameView.setGameStatusPanel(statusPanel);

            BorderLayout layout = (BorderLayout) gameView.getLayout();

            JPanel sidePanel = assertInstanceOf(
                    JPanel.class,
                    layout.getLayoutComponent(BorderLayout.EAST));

            BorderLayout sideLayout = (BorderLayout) sidePanel.getLayout();

            assertSame(statusPanel,
                    sideLayout.getLayoutComponent(BorderLayout.CENTER));

            assertNull(sideLayout.getLayoutComponent(BorderLayout.NORTH));
            assertEquals(1, sidePanel.getComponentCount());

            LongestRoadPanel embeddedPanel =
                    find(statusPanel, "longest-road-panel", LongestRoadPanel.class);

            assertNotNull(embeddedPanel);
            assertSame(originalPanel, embeddedPanel);
            assertTrue(SwingUtilities.isDescendingFrom(embeddedPanel, statusPanel));

            JPanel overviewCards = assertInstanceOf(
                    JPanel.class, embeddedPanel.getParent());

            BorderLayout cardsLayout = assertInstanceOf(
                    BorderLayout.class, overviewCards.getLayout());

            assertNotNull(cardsLayout.getLayoutComponent(BorderLayout.NORTH));
            assertSame(embeddedPanel,
                    cardsLayout.getLayoutComponent(BorderLayout.CENTER));

            JPanel overviewPanel = assertInstanceOf(
                    JPanel.class, overviewCards.getParent());

            BorderLayout overviewLayout = assertInstanceOf(
                    BorderLayout.class, overviewPanel.getLayout());

            assertSame(overviewCards,
                    overviewLayout.getLayoutComponent(BorderLayout.CENTER));

            JLabel playersLabel = assertInstanceOf(
                    JLabel.class,
                    overviewLayout.getLayoutComponent(BorderLayout.SOUTH));

            assertEquals("Players", playersLabel.getText());

            longestRoadViewModel.updateState("Alice", 6);

            assertEquals("Alice",
                    find(embeddedPanel, "longest-road-holder", JLabel.class).getText());

            assertEquals("6",
                    find(embeddedPanel, "longest-road-length", JLabel.class).getText());

            assertEquals("+2 VP",
                    find(embeddedPanel, "longest-road-bonus", JLabel.class).getText());
        });
    }
}
