package catan.usecase.weather;

import catan.entity.CityLocation;
import catan.entity.WeatherData;
import java.util.Objects;

/** Handles city selection and weather retrieval for the game. */
public class WeatherInteractor implements WeatherInputBoundary {

  private final WeatherDataAccessInterface weatherDataAccessObject;
  private final WeatherOutputBoundary weatherPresenter;

  /**
   * Constructs the weather interactor.
   *
   * @param weatherDataAccessObject the object used to retrieve weather data
   * @param weatherPresenter the weather output presenter
   */
  public WeatherInteractor(
          WeatherDataAccessInterface weatherDataAccessObject,
          WeatherOutputBoundary weatherPresenter) {

    this.weatherDataAccessObject = Objects.requireNonNull(
            weatherDataAccessObject, "weatherDataAccessObject");
    this.weatherPresenter = Objects.requireNonNull(
            weatherPresenter, "weatherPresenter");
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

    WeatherData weather;

    try {
      int weatherCode =
              weatherDataAccessObject.fetchWeatherCode(selectedCity);

      weather = WeatherData.fromWmoCode(weatherCode);

    } catch (WeatherUnavailableException exception) {
      weather = WeatherData.none();
    }

    WeatherOutputData outputData =
            new WeatherOutputData(selectedCity, weather);

    weatherPresenter.prepareSuccessView(outputData);
  }
}
