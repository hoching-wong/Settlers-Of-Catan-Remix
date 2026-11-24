package catan.framework.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.CityLocation;
import catan.usecase.weather.WeatherUnavailableException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/** Tests JSON parsing and HTTP responses in the Open-Meteo weather DAO. */
class OpenMeteoWeatherDataAccessObjectTest {

  private static final int SUCCESS_STATUS = 200;
  private static final int SERVER_ERROR_STATUS = 500;
  private static final String SUNNY_JSON = "{\"current\":{\"weather_code\":0}}";

  private HttpServer server;
  private final AtomicReference<String> requestedQuery = new AtomicReference<>();

  /** Stops the local HTTP server after each test. */
  @AfterEach
  void tearDown() {
    if (server != null) {
      server.stop(0);
    }
  }

  /** Tests that weather code 0 is parsed correctly. */
  @Test
  void testSunnyWeatherCode() {
    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(SUNNY_JSON);

    assertTrue(result.isPresent());
    assertEquals(0, result.getAsInt());
  }

  /** Tests that weather code 95 is parsed correctly. */
  @Test
  void testStormyWeatherCode() {
    String json = "{\"current\":{\"weather_code\":95}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertTrue(result.isPresent());
    assertEquals(95, result.getAsInt());
  }

  /** Tests a response with no weather code. */
  @Test
  void testMissingWeatherCode() {
    String json = "{\"current\":{}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  /** Tests a response with no current weather object. */
  @Test
  void testMissingCurrentObject() {
    String json = "{}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  /** Tests malformed JSON. */
  @Test
  void testInvalidJson() {
    String json = "invalid json";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  /** Tests a weather code with an invalid data type. */
  @Test
  void testInvalidWeatherCodeType() {
    String json = "{\"current\":{\"weather_code\":\"unknown\"}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  /**
   * Tests a successful HTTP request using city coordinates.
   *
   * @throws IOException if the local server cannot start
   * @throws WeatherUnavailableException if weather retrieval fails
   */
  @Test
  void testSuccessfulWeatherRequest() throws IOException, WeatherUnavailableException {

    startFakeServer(SUCCESS_STATUS, SUNNY_JSON);

    OpenMeteoWeatherDataAccessObject dao = createDao();
    CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);

    int result = dao.fetchWeatherCode(city);

    assertEquals(0, result);
    assertTrue(requestedQuery.get().contains("latitude=43.6532"));
    assertTrue(requestedQuery.get().contains("longitude=-79.3832"));
    assertTrue(requestedQuery.get().contains("current=weather_code"));
  }

  /**
   * Tests that an HTTP server error causes an exception.
   *
   * @throws IOException if the local server cannot start
   */
  @Test
  void testServerError() throws IOException {
    startFakeServer(SERVER_ERROR_STATUS, "{}");

    OpenMeteoWeatherDataAccessObject dao = createDao();
    CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);

    assertThrows(WeatherUnavailableException.class, () -> dao.fetchWeatherCode(city));
  }

  /**
   * Tests that invalid JSON causes an exception.
   *
   * @throws IOException if the local server cannot start
   */
  @Test
  void testInvalidApiResponse() throws IOException {
    startFakeServer(SUCCESS_STATUS, "invalid json");

    OpenMeteoWeatherDataAccessObject dao = createDao();
    CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);

    assertThrows(WeatherUnavailableException.class, () -> dao.fetchWeatherCode(city));
  }

  /**
   * Tests that a missing weather code causes an exception.
   *
   * @throws IOException if the local server cannot start
   */
  @Test
  void testMissingApiWeatherCode() throws IOException {
    startFakeServer(SUCCESS_STATUS, "{\"current\":{}}");

    OpenMeteoWeatherDataAccessObject dao = createDao();
    CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);

    assertThrows(WeatherUnavailableException.class, () -> dao.fetchWeatherCode(city));
  }

  /**
   * Starts a local server with a predefined response.
   *
   * @param statusCode the HTTP response status
   * @param responseBody the response content
   * @throws IOException if the server cannot start
   */
  private void startFakeServer(int statusCode, String responseBody) throws IOException {

    server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);

    server.createContext(
        "/forecast",
        exchange -> {
          requestedQuery.set(exchange.getRequestURI().getRawQuery());

          byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);

          exchange.sendResponseHeaders(statusCode, response.length);

          try (OutputStream output = exchange.getResponseBody()) {
            output.write(response);
          }
        });

    server.start();
  }

  /**
   * Creates a DAO pointing to the local test server.
   *
   * @return the weather DAO
   */
  private OpenMeteoWeatherDataAccessObject createDao() {
    String apiUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/forecast";

    return new OpenMeteoWeatherDataAccessObject(HttpClient.newHttpClient(), apiUrl);
  }
}
