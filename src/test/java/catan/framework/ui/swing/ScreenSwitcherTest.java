package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.flush;
import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

class ScreenSwitcherTest {
    private final ViewManagerModel viewManager = new ViewManagerModel();
    private final JPanel startMenu = new JPanel();
    private final JPanel game = new JPanel();
    private final JPanel gameOver = new JPanel();
    private final ScreenSwitcher switcher = new ScreenSwitcher(viewManager, startMenu, game, gameOver);

    private void assertOnlyVisible(JPanel expected) {
        for (JPanel panel : new JPanel[] {startMenu, game, gameOver}) {
            assertEquals(panel == expected, panel.isVisible());
        }
    }

    @Test
    void startsOnTheStartMenu() {
        assertOnlyVisible(startMenu);
    }

    @Test
    void followsTheViewManager() {
        viewManager.setActiveScreen(Screen.GAME);
        flush();
        assertOnlyVisible(game);

        viewManager.setActiveScreen(Screen.GAME_OVER);
        flush();
        assertOnlyVisible(gameOver);

        viewManager.setActiveScreen(Screen.START_MENU);
        flush();
        assertOnlyVisible(startMenu);
    }

    @Test
    void switchesWhenTheChangeComesFromABackgroundThread() throws InterruptedException {
        Thread worker = new Thread(() -> viewManager.setActiveScreen(Screen.GAME));
        worker.start();
        worker.join();
        flush();
        assertOnlyVisible(game);
    }

    @Test
    void startsOnWhicheverScreenIsAlreadyActive() {
        ViewManagerModel later = new ViewManagerModel();
        later.setActiveScreen(Screen.GAME_OVER);
        JPanel a = new JPanel();
        JPanel b = new JPanel();
        JPanel c = new JPanel();
        new ScreenSwitcher(later, a, b, c);
        assertTrue(c.isVisible());
        assertFalse(a.isVisible());
    }

    @Test
    void holdsAllThreeScreens() {
        assertEquals(3, switcher.getComponentCount());
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new ScreenSwitcher(null, startMenu, game, gameOver));
        assertThrows(NullPointerException.class, () -> new ScreenSwitcher(viewManager, null, game, gameOver));
        assertThrows(NullPointerException.class, () -> new ScreenSwitcher(viewManager, startMenu, null, gameOver));
        assertThrows(NullPointerException.class, () -> new ScreenSwitcher(viewManager, startMenu, game, null));
    }
}
