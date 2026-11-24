package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.ViewManagerModel;
import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Shows one screen at a time and switches when the {@link ViewManagerModel} changes. */
public class ScreenSwitcher extends JPanel implements PropertyChangeListener {
    private final ViewManagerModel viewManager;
    private final CardLayout cards = new CardLayout();

    public ScreenSwitcher(ViewManagerModel viewManager, JComponent startMenu, JComponent game,
                          JComponent gameOver) {
        this.viewManager = Objects.requireNonNull(viewManager, "viewManager");
        setLayout(cards);
        add(Objects.requireNonNull(startMenu, "startMenu"), Screen.START_MENU.name());
        add(Objects.requireNonNull(game, "game"), Screen.GAME.name());
        add(Objects.requireNonNull(gameOver, "gameOver"), Screen.GAME_OVER.name());
        viewManager.addPropertyChangeListener(this);
        showActiveScreen();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            showActiveScreen();
        } else {
            SwingUtilities.invokeLater(this::showActiveScreen);
        }
    }

    private void showActiveScreen() {
        cards.show(this, viewManager.getActiveScreen().name());
    }
}
