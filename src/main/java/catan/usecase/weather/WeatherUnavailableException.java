package catan.usecase.weather;

/** Represents an error that occurs when weather data is unavailable. */
public class WeatherUnavailableException extends Exception {

  /**
   * Creates an exception with a message.
   *
   * @param message the error message
   */
  public WeatherUnavailableException(String message) {
    super(message);
  }

  /**
   * Creates an exception with a message and its cause.
   *
   * @param message the error message
   * @param cause the original exception
   */
  public WeatherUnavailableException(String message, Throwable cause) {
    super(message, cause);
  }
}
