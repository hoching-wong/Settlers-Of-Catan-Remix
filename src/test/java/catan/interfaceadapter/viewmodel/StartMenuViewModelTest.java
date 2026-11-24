package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class StartMenuViewModelTest {
    private final StartMenuViewModel viewModel = new StartMenuViewModel(List.of("Toronto", "London"));
    private final List<String> events = new ArrayList<>();

    StartMenuViewModelTest() {
        viewModel.addPropertyChangeListener(e -> events.add(e.getPropertyName()));
    }

    @Test
    void startsEmptyWithTheCityList() {
        assertEquals(List.of("Toronto", "London"), viewModel.getCityNames());
        assertEquals("", viewModel.getErrorMessage());
        assertTrue(viewModel.getPlayerNames().isEmpty());
        assertEquals("", viewModel.getCityName());
        assertEquals("", viewModel.getWeatherText());
        assertEquals("", viewModel.getBonusText());
    }

    @Test
    void cityListIsACopyThatCannotBeChanged() {
        List<String> source = new ArrayList<>(List.of("Tokyo"));
        StartMenuViewModel model = new StartMenuViewModel(source);
        source.add("Paris");
        assertEquals(List.of("Tokyo"), model.getCityNames());
        assertThrows(UnsupportedOperationException.class, () -> model.getCityNames().add("Rome"));
    }

    @Test
    void settingAnErrorFiresEvenForTheSameMessage() {
        viewModel.setError("Please select a city.");
        viewModel.setError("Please select a city.");
        assertEquals("Please select a city.", viewModel.getErrorMessage());
        assertEquals(List.of(StartMenuViewModel.ERROR, StartMenuViewModel.ERROR), events);
    }

    @Test
    void aNullErrorBecomesEmpty() {
        viewModel.setError("x");
        viewModel.setError(null);
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void gameStartedStoresTheSummaryAndClearsTheError() {
        viewModel.setError("bad");
        events.clear();
        List<String> names = new ArrayList<>(List.of("Ann", "Bob"));
        viewModel.setGameStarted(names, "Toronto", "Weather in Toronto: Sunny", "Bonus: Lumber x2");
        names.add("late");

        assertEquals(List.of("Ann", "Bob"), viewModel.getPlayerNames());
        assertEquals("Toronto", viewModel.getCityName());
        assertEquals("Weather in Toronto: Sunny", viewModel.getWeatherText());
        assertEquals("Bonus: Lumber x2", viewModel.getBonusText());
        assertEquals("", viewModel.getErrorMessage());
        assertEquals(List.of(StartMenuViewModel.SUMMARY), events);
        assertThrows(UnsupportedOperationException.class, () -> viewModel.getPlayerNames().add("x"));
    }

    @Test
    void resetEmptiesEverything() {
        viewModel.setGameStarted(List.of("Ann", "Bob"), "Toronto", "w", "b");
        viewModel.setError("e");
        events.clear();
        viewModel.reset();

        assertTrue(viewModel.getPlayerNames().isEmpty());
        assertEquals("", viewModel.getCityName());
        assertEquals("", viewModel.getWeatherText());
        assertEquals("", viewModel.getBonusText());
        assertEquals("", viewModel.getErrorMessage());
        assertTrue(events.contains(StartMenuViewModel.SUMMARY));
        assertTrue(events.contains(StartMenuViewModel.ERROR));
        assertEquals(List.of("Toronto", "London"), viewModel.getCityNames(), "the city list stays");
    }
}
