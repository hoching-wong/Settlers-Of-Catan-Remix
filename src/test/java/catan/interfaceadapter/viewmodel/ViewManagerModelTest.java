package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.ViewManagerModel.Screen;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewManagerModelTest {
    private final ViewManagerModel viewModel = new ViewManagerModel();

    @Test
    void startsOnTheStartMenu() {
        assertEquals(Screen.START_MENU, viewModel.getActiveScreen());
    }

    @Test
    void changingTheScreenTellsTheListeners() {
        List<Object> seen = new ArrayList<>();
        viewModel.addPropertyChangeListener(e -> {
            assertEquals(ViewManagerModel.SCREEN, e.getPropertyName());
            seen.add(e.getNewValue());
        });
        viewModel.setActiveScreen(Screen.GAME);
        viewModel.setActiveScreen(Screen.GAME_OVER);
        viewModel.setActiveScreen(Screen.GAME_OVER);

        assertEquals(List.of(Screen.GAME, Screen.GAME_OVER, Screen.GAME_OVER), seen);
        assertEquals(Screen.GAME_OVER, viewModel.getActiveScreen());
    }

    @Test
    void rejectsNull() {
        assertThrows(NullPointerException.class, () -> viewModel.setActiveScreen(null));
        assertEquals(Screen.START_MENU, viewModel.getActiveScreen());
    }
}
