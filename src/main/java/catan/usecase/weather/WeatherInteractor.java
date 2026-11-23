package catan.usecase.weather;

import catan.entity.CityLocation;
import catan.entity.WeatherData;
import java.util.Objects;
import java.util.OptionalInt;

/**
 * Handles city selection and weather retrieval for the game.
 */
public class WeatherInteractor implements WeatherInputBoundary {

    private final WeatherDataAccessInterface weatherDataAccess;
    private final WeatherOutputBoundary weatherPresenter;

    /**
     * Constructs the weather interactor.
     *
     * @param weatherDataAccess the weather data access interface
     * @param weatherPresenter the weather output presenter
     */
    public WeatherInteractor(
            WeatherDataAccessInterface weatherDataAccess,
            WeatherOutputBoundary weatherPresenter) {
        this.weatherDataAccess =
                Objects.requireNonNull(weatherDataAccess);
        this.weatherPresenter =
                Objects.requireNonNull(weatherPresenter);
    }

    /**
     * Retrieves weather information for the selected city.
     *
     * @param inputData the selected city information
     */
    @Override
    public void execute(WeatherInputData inputData) {
        if (inputData == null
                || inputData.getCityName() == null
                || inputData.getCityName().isBlank()) {
            weatherPresenter.prepareFailView(
                    "Please select a city before starting the game.");
            return;
        }

        CityLocation selectedCity = null;

        for (CityLocation city : CityLocation.defaults()) {
            if (city.getName().equals(inputData.getCityName())) {
                selectedCity = city;
                break;
            }
        }

        if (selectedCity == null) {
            weatherPresenter.prepareFailView(
                    "The selected city is not available.");
            return;
        }

        OptionalInt weatherCode =
                weatherDataAccess.getWeatherCode(selectedCity);

        WeatherData weather;

        if (weatherCode.isPresent()) {
            weather = WeatherData.fromWmoCode(
                    weatherCode.getAsInt());
        } else {
            weather = WeatherData.none();
        }

        WeatherOutputData outputData =
                new WeatherOutputData(selectedCity, weather);

        weatherPresenter.prepareSuccessView(outputData);
    }
}
