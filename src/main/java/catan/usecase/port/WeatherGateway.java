package catan.usecase.port;

import catan.entity.CityLocation;

/** Defines the interface for retrieving weather data from an external weather service. */
public interface WeatherGateway {

  /**
   * Retrieves the current WMO weather code for a selected city.
   *
   * @param city the city whose weather data is requested
   * @return the current WMO weather code
   * @throws WeatherUnavailableException if the weather data cannot be retrieved
   */
  int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException;
}
