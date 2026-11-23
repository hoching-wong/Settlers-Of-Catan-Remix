package catan.interfaceadapters.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import catan.entity.CityLocation;
import catan.entity.WeatherData;
import catan.usecase.weather.WeatherOutputData;
import org.junit.jupiter.api.Test;

/**
 * Tests for WeatherPresenter.
 */
class WeatherPresenterTest {

    @Test
    void stormyWeatherUpdatesViewModel() {
        WeatherViewModel viewModel = new WeatherViewModel();
        WeatherPresenter presenter = new WeatherPresenter(viewModel);

        CityLocation city = new CityLocation("Paris", 48.8566, 2.3522);
        WeatherData weather = WeatherData.fromWmoCode(95);
        WeatherOutputData outputData = new WeatherOutputData(city, weather);

        presenter.prepareSuccessView(outputData);

        assertEquals("Paris", viewModel.getCityName());
        assertEquals("Stormy", viewModel.getWeatherCondition());
        assertEquals("Ore ×2", viewModel.getBonusDescription());
        assertEquals("", viewModel.getErrorMessage());
    }

    @Test
    void sunnyWeatherUpdatesViewModel() {
        WeatherViewModel viewModel = new WeatherViewModel();
        WeatherPresenter presenter = new WeatherPresenter(viewModel);

        CityLocation city = new CityLocation("Sydney", -33.8688, 151.2093);
        WeatherData weather = WeatherData.fromWmoCode(0);
        WeatherOutputData outputData = new WeatherOutputData(city, weather);

        presenter.prepareSuccessView(outputData);

        assertEquals("Sydney", viewModel.getCityName());
        assertEquals("Sunny", viewModel.getWeatherCondition());
        assertEquals("Lumber ×2", viewModel.getBonusDescription());
    }

    @Test
    void fogWeatherHasNoBonus() {
        WeatherViewModel viewModel = new WeatherViewModel();
        WeatherPresenter presenter = new WeatherPresenter(viewModel);

        CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);
        WeatherData weather = WeatherData.fromWmoCode(45);
        WeatherOutputData outputData = new WeatherOutputData(city, weather);

        presenter.prepareSuccessView(outputData);

        assertEquals("Fog", viewModel.getWeatherCondition());
        assertEquals("No weather bonus", viewModel.getBonusDescription());
    }

    @Test
    void unavailableWeatherHasNoBonus() {
        WeatherViewModel viewModel = new WeatherViewModel();
        WeatherPresenter presenter = new WeatherPresenter(viewModel);

        CityLocation city = new CityLocation("London", 51.5072, -0.1276);
        WeatherData weather = WeatherData.none();
        WeatherOutputData outputData = new WeatherOutputData(city, weather);

        presenter.prepareSuccessView(outputData);

        assertEquals("London", viewModel.getCityName());
        assertEquals("Unavailable", viewModel.getWeatherCondition());
        assertEquals("No weather bonus", viewModel.getBonusDescription());
    }

    @Test
    void failureUpdatesErrorMessage() {
        WeatherViewModel viewModel = new WeatherViewModel();
        WeatherPresenter presenter = new WeatherPresenter(viewModel);

        presenter.prepareFailView(
                "Please select a city before starting the game.");

        assertEquals(
                "Please select a city before starting the game.",
                viewModel.getErrorMessage());
    }

    @Test
    void constructorRejectsNullViewModel() {
        assertThrows(
                NullPointerException.class,
                () -> new WeatherPresenter(null));
    }
}

