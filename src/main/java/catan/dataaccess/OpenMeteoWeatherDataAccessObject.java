package catan.dataaccess;

import catan.entity.CityLocation;
import catan.usecase.weather.WeatherDataAccessInterface;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.OptionalInt;
import org.json.JSONException;
import org.json.JSONObject;

/** Retrieves current weather codes from the Open-Meteo Weather API. */
public class OpenMeteoWeatherDataAccessObject implements WeatherDataAccessInterface {

  private static final String API_URL = "https://api.open-meteo.com/v1/forecast";
  private static final int TIMEOUT_SECONDS = 10;
  private static final int SUCCESS_STATUS_CODE = 200;

  private final HttpClient client;

  /** Constructs a weather data access object with an HTTP client. */
  public OpenMeteoWeatherDataAccessObject() {
    client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS)).build();
  }

  /**
   * Retrieves the current WMO weather code for the selected city.
   *
   * @param city the city whose weather should be retrieved
   * @return the weather code, or an empty result if the request fails
   */
  @Override
  public OptionalInt getWeatherCode(CityLocation city) {
    String url =
        API_URL
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
        return OptionalInt.empty();
      }

      return parseWeatherCode(response.body());

    } catch (IOException | IllegalArgumentException exception) {
      return OptionalInt.empty();

    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return OptionalInt.empty();
    }
  }

  /**
   * Extracts the WMO weather code from a JSON response.
   *
   * @param responseBody the JSON response returned by the API
   * @return the weather code, or an empty result if the response does not contain a valid weather
   *     code
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
