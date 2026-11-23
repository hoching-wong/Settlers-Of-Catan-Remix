package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static catan.framework.ui.swing.SwingTestSupport.flush;
import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.controller.RestartGameController;
import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import catan.usecase.restartgame.RestartGameInputData;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import org.junit.jupiter.api.Test;

class GameOverViewTest {
    private final GameOverViewModel viewModel = new GameOverViewModel();
    private final List<RestartGameInputData> clicks = new ArrayList<>();
    private final GameOverView view = new GameOverView(viewModel, new RestartGameController(clicks::add));

    private String text(String name) {
        return find(view, name, JLabel.class).getText();
    }

    @Test
    void startsBlankWithTheBackToMenuButton() {
        assertEquals(" ", text("winner"));
        assertNull(find(view, "score0", JLabel.class));
        assertEquals("Back to Main Menu", find(view, "backToMenu", JButton.class).getText());
    }

    @Test
    void showsTheWinnerAndEveryScore() {
        viewModel.setResult("Bob", List.of(new ScoreEntry("Ann", 6, false), new ScoreEntry("Bob", 10, true),
                new ScoreEntry("Cat", 1, false)));
        flush();

        assertEquals("Winner: Bob", text("winner"));
        assertEquals("Ann: 6 points", text("score0"));
        assertEquals("Bob: 10 points  (winner)", text("score1"));
        assertEquals("Cat: 1 point", text("score2"));
        assertNull(find(view, "score3", JLabel.class));
    }

    @Test
    void aNewResultReplacesTheOldRows() {
        viewModel.setResult("Ann", List.of(new ScoreEntry("Ann", 10, true), new ScoreEntry("Bob", 2, false),
                new ScoreEntry("Cat", 2, false)));
        flush();
        viewModel.setResult("Dan", List.of(new ScoreEntry("Dan", 11, true), new ScoreEntry("Eve", 5, false)));
        flush();

        assertEquals("Dan: 11 points  (winner)", text("score0"));
        assertEquals("Eve: 5 points", text("score1"));
        assertNull(find(view, "score2", JLabel.class));
    }

    @Test
    void backToMenuButtonCallsTheController() {
        find(view, "backToMenu", JButton.class).doClick();
        assertEquals(List.of(new RestartGameInputData()), clicks);
    }

    @Test
    void anErrorIsShown() {
        viewModel.setError("The game is not over yet.");
        flush();
        assertEquals("The game is not over yet.", text("gameOverError"));
    }

    @Test
    void resettingClearsTheScreen() {
        viewModel.setResult("Ann", List.of(new ScoreEntry("Ann", 10, true)));
        viewModel.setError("e");
        viewModel.reset();
        flush();
        assertEquals(" ", text("winner"));
        assertEquals(" ", text("gameOverError"));
        assertNull(find(view, "score0", JLabel.class));
    }

    @Test
    void updatesFromABackgroundThreadAreShown() throws InterruptedException {
        Thread worker = new Thread(() -> viewModel.setResult("Ann", List.of(new ScoreEntry("Ann", 10, true))));
        worker.start();
        worker.join();
        flush();
        assertEquals("Winner: Ann", text("winner"));
    }

    @Test
    void constructorRejectsNulls() {
        RestartGameController controller = new RestartGameController(clicks::add);
        assertThrows(NullPointerException.class, () -> new GameOverView(null, controller));
        assertThrows(NullPointerException.class, () -> new GameOverView(viewModel, null));
    }
}
