package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static catan.framework.ui.swing.SwingTestSupport.flush;
import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.endturn.EndTurnInputData;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
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
}
