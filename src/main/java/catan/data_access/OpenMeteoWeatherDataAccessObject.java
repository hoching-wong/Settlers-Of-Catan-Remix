package catan.data_access;

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

      if (response.statusCode() != 200) {
        return OptionalInt.empty();
      }

      JSONObject json = new JSONObject(response.body());
      int weatherCode = json.getJSONObject("current").getInt("weather_code");

      return OptionalInt.of(weatherCode);

    } catch (IOException | JSONException | IllegalArgumentException exception) {
      return OptionalInt.empty();

    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      return OptionalInt.empty();
    }
  }
}
