package catan.usecase.weather;

import catan.entity.CityLocation;

/** Defines the interface for retrieving weather data from an external weather service. */
public interface WeatherDataAccessInterface {

  /**
   * Retrieves the current WMO weather code for a selected city.
   *
   * @param city the city whose weather data is requested
   * @return the current WMO weather code
   * @throws WeatherUnavailableException if the weather data cannot be retrieved
   */
  int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException;
}
