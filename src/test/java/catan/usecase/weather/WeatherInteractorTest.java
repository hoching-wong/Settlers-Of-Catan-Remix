package catan.usecase.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.CityLocation;
import catan.entity.ResourceType;
import catan.entity.WeatherType;
import org.junit.jupiter.api.Test;

/** Tests the WeatherInteractor. */
class WeatherInteractorTest {

  /** Tests that a valid city retrieves the correct weather. */
  @Test
  void validCityRetrievesWeather() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(95);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("Paris"));

    assertEquals("Paris",
            weatherDataAccessObject.lastRequestedCity.getName());
    assertEquals(1, weatherDataAccessObject.callCount);
    assertNotNull(outputData);
    assertEquals("Paris", outputData.getCity().getName());
    assertEquals(WeatherType.STORMY,
            outputData.getWeather().getCondition());
    assertEquals(ResourceType.ORE,
            outputData.getWeather().getBonusResource());
    assertEquals(2, outputData.getWeather().getMultiplier());
    assertTrue(outputData.isWeatherAvailable());
  }

  /** Tests that null input produces an error. */
  @Test
  void nullInputShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interactor.execute(null));

    assertEquals("Please select a city.", exception.getMessage());
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that an empty city name produces an error. */
  @Test
  void blankCityShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interactor.execute(new WeatherInputData("")));

    assertEquals("Please select a city.", exception.getMessage());
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that a null city name produces an error. */
  @Test
  void nullCityNameShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interactor.execute(new WeatherInputData(null)));

    assertEquals("Please select a city.", exception.getMessage());
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that whitespace-only city names are rejected. */
  @Test
  void whitespaceCityShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interactor.execute(new WeatherInputData("   ")));

    assertEquals("Please select a city.", exception.getMessage());
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that an unknown city is rejected. */
  @Test
  void unknownCityShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> interactor.execute(new WeatherInputData("Berlin")));

    assertEquals("Unknown city: Berlin.", exception.getMessage());
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that API failure produces weather without a bonus. */
  @Test
  void apiFailureContinuesWithoutBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(null);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("Toronto"));

    assertEquals(1, weatherDataAccessObject.callCount);
    assertNotNull(outputData);
    assertEquals("Toronto", outputData.getCity().getName());
    assertFalse(outputData.isWeatherAvailable());
    assertFalse(outputData.getWeather().hasBonus());
    assertEquals(1, outputData.getWeather().getMultiplier());
  }

  /** Tests that sunny weather produces the correct bonus. */
  @Test
  void recognizedWeatherProvidesBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("Sydney"));

    assertNotNull(outputData);
    assertTrue(outputData.isWeatherAvailable());
    assertEquals(WeatherType.SUNNY,
            outputData.getWeather().getCondition());
    assertEquals(ResourceType.LUMBER,
            outputData.getWeather().getBonusResource());
    assertTrue(outputData.getWeather().hasBonus());
  }

  /** Tests that fog does not provide a resource bonus. */
  @Test
  void fogWeatherHasNoBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(45);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("London"));

    assertNotNull(outputData);
    assertTrue(outputData.isWeatherAvailable());
    assertEquals(WeatherType.FOG,
            outputData.getWeather().getCondition());
    assertFalse(outputData.getWeather().hasBonus());
    assertEquals(1, outputData.getWeather().getMultiplier());
  }

  /** Tests that an unrecognized weather code has no bonus. */
  @Test
  void unknownWeatherCodeHasNoBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(1000);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("Toronto"));

    assertTrue(outputData.isWeatherAvailable());
    assertNull(outputData.getWeather().getCondition());
    assertFalse(outputData.getWeather().hasBonus());
    assertEquals(1, outputData.getWeather().getMultiplier());
  }

  /** Tests that city matching ignores case and surrounding spaces. */
  @Test
  void cityMatchingIgnoresCaseAndSpaces() {
    FakeWeatherDataAccessObject weatherDataAccessObject =
            new FakeWeatherDataAccessObject(0);

    WeatherInteractor interactor =
            new WeatherInteractor(weatherDataAccessObject);

    WeatherOutputData outputData =
            interactor.execute(new WeatherInputData("  toronto "));

    assertEquals("Toronto", outputData.getCity().getName());
    assertEquals("Toronto",
            weatherDataAccessObject.lastRequestedCity.getName());
    assertEquals(1, weatherDataAccessObject.callCount);
  }

  /** Provides predefined weather codes without accessing the network. */
  private static class FakeWeatherDataAccessObject
          implements WeatherDataAccessInterface {

    private final Integer weatherCode;
    private CityLocation lastRequestedCity;
    private int callCount;

    /**
     * Constructs a fake weather data access object.
     *
     * @param weatherCode the code to return, or null to simulate failure
     */
    FakeWeatherDataAccessObject(Integer weatherCode) {
      this.weatherCode = weatherCode;
    }

    /**
     * Returns the configured weather code.
     *
     * @param city the requested city
     * @return the configured weather code
     * @throws WeatherUnavailableException if failure is simulated
     */
    @Override
    public int fetchWeatherCode(CityLocation city)
            throws WeatherUnavailableException {

      lastRequestedCity = city;
      callCount++;

      if (weatherCode == null) {
        throw new WeatherUnavailableException(
                "Weather service unavailable");
      }

      return weatherCode;
    }
  }
}
