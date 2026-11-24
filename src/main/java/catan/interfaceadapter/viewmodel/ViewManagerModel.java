package catan.interfaceadapter.viewmodel;

import java.util.Objects;

/** Which screen is showing. Presenters change it and the window switches to match. */
public class ViewManagerModel extends ViewModel {
    public static final String SCREEN = "screen";

    public enum Screen {
        START_MENU,
        GAME,
        GAME_OVER
    }

    private Screen activeScreen = Screen.START_MENU;

    public Screen getActiveScreen() {
        return activeScreen;
    }

    public void setActiveScreen(Screen screen) {
        this.activeScreen = Objects.requireNonNull(screen, "screen");
        firePropertyChanged(SCREEN, screen);
    }
}
