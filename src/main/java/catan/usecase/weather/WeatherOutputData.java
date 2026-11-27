package catan.usecase.weather;

import catan.entity.CityLocation;
import catan.entity.WeatherData;

/** Contains the selected city and its weather data. */
public class WeatherOutputData {

  private final CityLocation city;
  private final WeatherData weather;
  private final boolean weatherAvailable;

  /**
   * Creates the output data for the weather use case.
   *
   * @param city the selected city
   * @param weather the weather data for the city
   * @param weatherAvailable whether the weather request succeeded
   */
  public WeatherOutputData(
          CityLocation city,
          WeatherData weather,
          boolean weatherAvailable) {
    this.city = city;
    this.weather = weather;
    this.weatherAvailable = weatherAvailable;
  }

  /**
   * Returns the selected city.
   *
   * @return the selected city
   */
  public CityLocation getCity() {
    return city;
  }

  /**
   * Returns the weather data.
   *
   * @return the weather data
   */
  public WeatherData getWeather() {
    return weather;
  }

  /**
   * Returns whether the weather request succeeded.
   *
   * @return whether weather data is available
   */
  public boolean isWeatherAvailable() {
    return weatherAvailable;
  }
}
