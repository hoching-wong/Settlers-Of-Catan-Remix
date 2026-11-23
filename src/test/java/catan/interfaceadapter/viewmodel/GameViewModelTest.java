package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameViewModelTest {
    private final GameViewModel viewModel = new GameViewModel();
    private final List<String> events = new ArrayList<>();

    GameViewModelTest() {
        viewModel.addPropertyChangeListener(e -> events.add(e.getPropertyName()));
    }

    @Test
    void startsEmpty() {
        assertTrue(viewModel.getPlayerNames().isEmpty());
        assertEquals("", viewModel.getCurrentPlayerName());
        assertEquals("", viewModel.getInstruction());
        assertEquals("", viewModel.getMessage());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void startGameSetsPlayersAndTheFirstTurn() {
        List<String> names = new ArrayList<>(List.of("Ann", "Bob"));
        viewModel.startGame(names, "Ann", "Place a settlement");
        names.add("late");

        assertEquals(List.of("Ann", "Bob"), viewModel.getPlayerNames());
        assertEquals("Ann", viewModel.getCurrentPlayerName());
        assertEquals("Place a settlement", viewModel.getInstruction());
        assertEquals(List.of(GameViewModel.PLAYERS, GameViewModel.TURN), events);
        assertThrows(UnsupportedOperationException.class, () -> viewModel.getPlayerNames().add("x"));
    }

    @Test
    void startGameClearsOldMessages() {
        viewModel.setMessage("old");
        viewModel.setError("old error");
        viewModel.startGame(List.of("Ann", "Bob"), "Ann", "Roll the dice");
        assertEquals("", viewModel.getMessage());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void settingTheTurnClearsTheErrorButKeepsTheMessage() {
        viewModel.setMessage("note");
        viewModel.setError("bad");
        events.clear();
        viewModel.setTurn("Bob", "Roll the dice");

        assertEquals("Bob", viewModel.getCurrentPlayerName());
        assertEquals("Roll the dice", viewModel.getInstruction());
        assertEquals("", viewModel.getErrorMessage());
        assertEquals("note", viewModel.getMessage());
        assertEquals(List.of(GameViewModel.TURN), events);
    }

    @Test
    void messageAndErrorFireEachTimeAndTurnNullIntoEmpty() {
        viewModel.setMessage("a");
        viewModel.setMessage("a");
        viewModel.setError("b");
        viewModel.setError(null);
        viewModel.setMessage(null);
        assertEquals("", viewModel.getMessage());
        assertEquals("", viewModel.getErrorMessage());
        assertEquals(List.of(GameViewModel.MESSAGE, GameViewModel.MESSAGE, GameViewModel.ERROR,
                GameViewModel.ERROR, GameViewModel.MESSAGE), events);
    }

    @Test
    void resetEmptiesEverythingAndTellsTheView() {
        viewModel.startGame(List.of("Ann", "Bob"), "Ann", "x");
        viewModel.setMessage("m");
        viewModel.setError("e");
        events.clear();
        viewModel.reset();

        assertTrue(viewModel.getPlayerNames().isEmpty());
        assertEquals("", viewModel.getCurrentPlayerName());
        assertEquals("", viewModel.getInstruction());
        assertEquals("", viewModel.getMessage());
        assertEquals("", viewModel.getErrorMessage());
        assertEquals(List.of(GameViewModel.PLAYERS, GameViewModel.TURN, GameViewModel.MESSAGE,
                GameViewModel.ERROR), events);
    }

    @Test
    void removedListenersHearNothing() {
        List<String> heard = new ArrayList<>();
        java.beans.PropertyChangeListener listener = e -> heard.add(e.getPropertyName());
        viewModel.addPropertyChangeListener(listener);
        viewModel.removePropertyChangeListener(listener);
        viewModel.setMessage("x");
        assertTrue(heard.isEmpty());
    }
}
