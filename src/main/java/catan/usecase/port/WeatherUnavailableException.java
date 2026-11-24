package catan.usecase.port;

/** Thrown by a {@link WeatherGateway} when the weather could not be retrieved. */
public class WeatherUnavailableException extends Exception {

    public WeatherUnavailableException(String message) {
        super(message);
    }

    public WeatherUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
