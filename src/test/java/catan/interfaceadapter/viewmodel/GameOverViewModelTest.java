package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GameOverViewModelTest {
    private final GameOverViewModel viewModel = new GameOverViewModel();
    private final List<String> events = new ArrayList<>();

    GameOverViewModelTest() {
        viewModel.addPropertyChangeListener(e -> events.add(e.getPropertyName()));
    }

    @Test
    void startsEmpty() {
        assertEquals("", viewModel.getWinnerName());
        assertTrue(viewModel.getScores().isEmpty());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void storesTheResultAndClearsTheError() {
        viewModel.setError("old");
        events.clear();
        List<ScoreEntry> scores = new ArrayList<>(List.of(new ScoreEntry("Ann", 10, true),
                new ScoreEntry("Bob", 4, false)));
        viewModel.setResult("Ann", scores);
        scores.clear();

        assertEquals("Ann", viewModel.getWinnerName());
        assertEquals(2, viewModel.getScores().size());
        assertEquals(new ScoreEntry("Ann", 10, true), viewModel.getScores().get(0));
        assertEquals("", viewModel.getErrorMessage());
        assertEquals(List.of(GameOverViewModel.RESULT), events);
        assertThrows(UnsupportedOperationException.class, () -> viewModel.getScores().clear());
    }

    @Test
    void errorsFireEachTimeAndNullBecomesEmpty() {
        viewModel.setError("x");
        viewModel.setError("x");
        assertEquals(List.of(GameOverViewModel.ERROR, GameOverViewModel.ERROR), events);
        viewModel.setError(null);
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void resetEmptiesEverything() {
        viewModel.setResult("Ann", List.of(new ScoreEntry("Ann", 10, true)));
        viewModel.setError("e");
        events.clear();
        viewModel.reset();

        assertEquals("", viewModel.getWinnerName());
        assertTrue(viewModel.getScores().isEmpty());
        assertEquals("", viewModel.getErrorMessage());
        assertEquals(List.of(GameOverViewModel.RESULT, GameOverViewModel.ERROR), events);
    }
}
