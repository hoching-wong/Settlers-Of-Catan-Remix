package catan.usecase.weather;

import catan.entity.CityLocation;
import catan.entity.WeatherData;
import java.util.Objects;

/** Handles city selection and weather retrieval for the game. */
public class WeatherInteractor implements WeatherInputBoundary {

  private final WeatherDataAccessInterface weatherDataAccessObject;

  /**
   * Constructs the weather interactor.
   *
   * @param weatherDataAccessObject the object used to retrieve weather data
   */
  public WeatherInteractor(
          WeatherDataAccessInterface weatherDataAccessObject) {

    this.weatherDataAccessObject = Objects.requireNonNull(
            weatherDataAccessObject, "weatherDataAccessObject");
  }

  /**
   * Retrieves weather information for the selected city.
   *
   * @param inputData the selected city information
   * @return the result of the weather lookup
   * @throws IllegalArgumentException if the selected city is invalid
   */
  @Override
  public WeatherOutputData execute(WeatherInputData inputData) {

    if (inputData == null
            || inputData.getCityName() == null
            || inputData.getCityName().isBlank()) {

      throw new IllegalArgumentException("Please select a city.");
    }

    String cityName = inputData.getCityName().trim();

    CityLocation selectedCity = null;

    for (CityLocation city : CityLocation.defaults()) {
      if (city.getName().equalsIgnoreCase(cityName)) {
        selectedCity = city;
        break;
      }
    }

    if (selectedCity == null) {
      throw new IllegalArgumentException(
              "Unknown city: " + cityName + ".");
    }

    try {
      int weatherCode =
              weatherDataAccessObject.fetchWeatherCode(selectedCity);

      WeatherData weather =
              WeatherData.fromWmoCode(weatherCode);

      return new WeatherOutputData(
              selectedCity, weather, true);

    } catch (WeatherUnavailableException exception) {

      return new WeatherOutputData(
              selectedCity, WeatherData.none(), false);
    }
  }
}
