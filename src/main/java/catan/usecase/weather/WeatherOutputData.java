package catan.usecase.weather;

import catan.entity.CityLocation;
import catan.entity.WeatherData;

/** Contains the selected city and its weather data. */
public class WeatherOutputData {

  private final CityLocation city;
  private final WeatherData weather;

  /**
   * Creates the output data for the weather use case.
   *
   * @param city the selected city
   * @param weather the weather data for the city
   */
  public WeatherOutputData(CityLocation city, WeatherData weather) {
    this.city = city;
    this.weather = weather;
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
}
