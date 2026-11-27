package catan.usecase.weather;

/** Contains the city selected by the player. */
public class WeatherInputData {

  private final String cityName;

  /**
   * Creates input data for the weather use case.
   *
   * @param cityName the selected city name
   */
  public WeatherInputData(String cityName) {
    this.cityName = cityName;
  }

  /**
   * Returns the selected city name.
   *
   * @return the city name
   */
  public String getCityName() {
    return cityName;
  }
}
