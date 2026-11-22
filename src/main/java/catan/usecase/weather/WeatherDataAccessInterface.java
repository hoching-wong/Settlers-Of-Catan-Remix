package catan.usecase.weather;

import catan.entity.CityLocation;
import java.util.OptionalInt;

/** Defines how the weather use case retrieves weather codes for selected cities. */
public interface WeatherDataAccessInterface {

  /**
   * Retrieves the current WMO weather code for a city.
   *
   * @param city the city whose weather should be retrieved
   * @return the WMO weather code, or an empty result if the weather cannot be retrieved
   */
  OptionalInt getWeatherCode(CityLocation city);
}
