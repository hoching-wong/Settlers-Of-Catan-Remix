package catan.framework.weather;

import catan.entity.CityLocation;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherUnavailableException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.OptionalInt;
import org.json.JSONException;
import org.json.JSONObject;

/** Retrieves current weather codes from the Open-Meteo Weather API. */
public class OpenMeteoWeatherDataAccessObject implements WeatherDataAccessInterface {

  private static final String API_URL = "https://api.open-meteo.com/v1/forecast";
  private static final int TIMEOUT_SECONDS = 10;
  private static final int SUCCESS_STATUS_CODE = 200;

  private final HttpClient client;
  private final String apiUrl;

  /** Creates a weather DAO using the real Open-Meteo API. */
  public OpenMeteoWeatherDataAccessObject() {
    this(
        HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS)).build(),
        API_URL);
  }

  /**
   * Creates a weather DAO with a configurable API endpoint.
   *
   * @param client the HTTP client used to send requests
   * @param apiUrl the weather API endpoint
   */
  OpenMeteoWeatherDataAccessObject(HttpClient client, String apiUrl) {
    this.client = Objects.requireNonNull(client, "client");
    this.apiUrl = Objects.requireNonNull(apiUrl, "apiUrl");
  }

  /**
   * Retrieves the current WMO weather code for the selected city.
   *
   * @param city the city whose weather should be retrieved
   * @return the current WMO weather code
   * @throws WeatherUnavailableException if weather cannot be retrieved
   */
  @Override
  public int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException {

    String url =
        apiUrl
            + "?latitude="
            + city.getLatitude()
            + "&longitude="
            + city.getLongitude()
            + "&current=weather_code";

    try {
      HttpRequest request =
          HttpRequest.newBuilder()
              .uri(URI.create(url))
              .timeout(Duration.ofSeconds(TIMEOUT_SECONDS))
              .GET()
              .build();

      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

      if (response.statusCode() != SUCCESS_STATUS_CODE) {
        throw new WeatherUnavailableException("Weather API returned HTTP " + response.statusCode());
      }

      OptionalInt weatherCode = parseWeatherCode(response.body());

      if (weatherCode.isEmpty()) {
        throw new WeatherUnavailableException("Invalid weather API response");
      }

      return weatherCode.getAsInt();

    } catch (IOException | IllegalArgumentException exception) {
      throw new WeatherUnavailableException("Failed to retrieve weather data", exception);

    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new WeatherUnavailableException("Weather request interrupted", exception);
    }
  }

  /**
   * Extracts the WMO weather code from a JSON response.
   *
   * @param responseBody the JSON response returned by the API
   * @return the weather code, or an empty result if invalid
   */
  static OptionalInt parseWeatherCode(String responseBody) {
    try {
      JSONObject json = new JSONObject(responseBody);
      int weatherCode = json.getJSONObject("current").getInt("weather_code");

      return OptionalInt.of(weatherCode);

    } catch (JSONException | IllegalArgumentException exception) {
      return OptionalInt.empty();
    }
  }
}
