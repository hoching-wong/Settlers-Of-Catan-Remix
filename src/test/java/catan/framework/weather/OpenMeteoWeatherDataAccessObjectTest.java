package catan.framework.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.OptionalInt;

import org.junit.jupiter.api.Test;

/** Tests JSON parsing in the Open-Meteo weather data access object. */
class OpenMeteoWeatherDataAccessObjectTest {

  @Test
  void testSunnyWeatherCode() {
    String json = "{\"current\":{\"weather_code\":0}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertTrue(result.isPresent());
    assertEquals(0, result.getAsInt());
  }

  @Test
  void testStormyWeatherCode() {
    String json = "{\"current\":{\"weather_code\":95}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertTrue(result.isPresent());
    assertEquals(95, result.getAsInt());
  }

  @Test
  void testMissingWeatherCode() {
    String json = "{\"current\":{}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  @Test
  void testMissingCurrentObject() {
    String json = "{}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  @Test
  void testInvalidJson() {
    String json = "invalid json";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }

  @Test
  void testInvalidWeatherCodeType() {
    String json = "{\"current\":{\"weather_code\":\"unknown\"}}";

    OptionalInt result = OpenMeteoWeatherDataAccessObject.parseWeatherCode(json);

    assertFalse(result.isPresent());
  }
}
