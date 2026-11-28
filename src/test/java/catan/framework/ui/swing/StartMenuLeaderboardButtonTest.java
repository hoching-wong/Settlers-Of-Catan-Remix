package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import catan.interfaceadapter.controller.LeaderboardController;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.usecase.startgame.StartGameInputData;
import java.awt.GraphicsEnvironment;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;

/** The Leaderboard button of the start menu. Drives the real components without showing a window. */
class StartMenuLeaderboardButtonTest {

    private final StartMenuViewModel viewModel =
            new StartMenuViewModel(List.of("Toronto", "London"));
    private final List<StartGameInputData> started = new ArrayList<>();
    private final StartMenuView view =
            new StartMenuView(viewModel, new StartGameController(started::add), Runnable::run);

    private final List<String> calls = new ArrayList<>();
    private final List<Window> openedOver = new ArrayList<>();
    private final LeaderboardController controller = new LeaderboardController(data -> calls.add("show"));
    private final Consumer<Window> opener = window -> {
        calls.add("open");
        openedOver.add(window);
    };

    private JButton button() {
        return find(view, "leaderboard", JButton.class);
    }

    @Test
    void theButtonIsHiddenUntilTheLeaderboardIsSetUp() {
        assertNotNull(button());
        assertFalse(button().isVisible());
    }

    @Test
    void afterSetUpTheButtonIsVisibleAndEnabled() {
        view.setLeaderboard(controller, opener);

        assertTrue(button().isVisible());
        assertTrue(button().isEnabled());
        assertEquals("Leaderboard", button().getText());
    }

    @Test
    void pressingLoadsTheLeaderboardFirstAndOpensTheDialogLast() {
        view.setLeaderboard(controller, opener);

        button().doClick();

        assertEquals(List.of("show", "open"), calls);
    }

    @Test
    void everyPressLoadsAgain() {
        view.setLeaderboard(controller, opener);

        button().doClick();
        button().doClick();

        assertEquals(List.of("show", "open", "show", "open"), calls);
    }

    @Test
    void opensOverNoWindowWhenTheViewIsNotInOne() {
        view.setLeaderboard(controller, opener);

        button().doClick();

        assertEquals(1, openedOver.size());
        assertNull(openedOver.get(0));
    }

    @Test
    void opensOverTheWindowThatHoldsTheView() {
        assumeFalse(GraphicsEnvironment.isHeadless());
        JFrame frame = new JFrame();
        try {
            frame.add(view);
            view.setLeaderboard(controller, opener);

            button().doClick();

            assertSame(frame, openedOver.get(0));
        } finally {
            frame.dispose();
        }
    }

    @Test
    void pressingDoesNotStartAGameOrChangeTheForm() {
        view.setLeaderboard(controller, opener);
        find(view, "name1", JTextField.class).setText("Ann");

        button().doClick();

        assertTrue(started.isEmpty());
        assertEquals("Ann", find(view, "name1", JTextField.class).getText());
        assertTrue(find(view, "start", JButton.class).isEnabled());
    }

    @Test
    void setLeaderboardRejectsNulls() {
        assertThrows(NullPointerException.class, () -> view.setLeaderboard(null, opener));
        assertThrows(NullPointerException.class, () -> view.setLeaderboard(controller, null));
    }
}
