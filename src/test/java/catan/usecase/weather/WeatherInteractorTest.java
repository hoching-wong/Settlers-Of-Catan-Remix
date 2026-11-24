package catan.usecase.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(95);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData("Paris"));

    assertEquals("Paris", weatherDataAccessObject.lastRequestedCity.getName());
    assertEquals(1, weatherDataAccessObject.callCount);
    assertNotNull(presenter.successData);
    assertEquals("Paris", presenter.successData.getCity().getName());
    assertEquals(WeatherType.STORMY, presenter.successData.getWeather().getCondition());
    assertEquals(ResourceType.ORE, presenter.successData.getWeather().getBonusResource());
    assertEquals(2, presenter.successData.getWeather().getMultiplier());
    assertNull(presenter.errorMessage);
  }

  /** Tests that null input produces an error. */
  @Test
  void nullInputShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(0);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(null);

    assertEquals("Please select a city before starting the game.", presenter.errorMessage);
    assertNull(presenter.successData);
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that an empty city name produces an error. */
  @Test
  void blankCityShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(0);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData(""));

    assertEquals("Please select a city before starting the game.", presenter.errorMessage);
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that an unknown city is rejected. */
  @Test
  void unknownCityShowsError() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(0);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData("Berlin"));

    assertEquals("The selected city is not available.", presenter.errorMessage);
    assertNull(presenter.successData);
    assertEquals(0, weatherDataAccessObject.callCount);
  }

  /** Tests that API failure produces weather without a bonus. */
  @Test
  void apiFailureContinuesWithoutBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(null);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData("Toronto"));

    assertEquals(1, weatherDataAccessObject.callCount);
    assertNotNull(presenter.successData);
    assertEquals("Toronto", presenter.successData.getCity().getName());
    assertFalse(presenter.successData.getWeather().hasBonus());
    assertEquals(1, presenter.successData.getWeather().getMultiplier());
    assertNull(presenter.errorMessage);
  }

  /** Tests that sunny weather produces the correct bonus. */
  @Test
  void recognizedWeatherProvidesBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(0);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData("Sydney"));

    assertNotNull(presenter.successData);
    assertEquals(WeatherType.SUNNY, presenter.successData.getWeather().getCondition());
    assertEquals(ResourceType.LUMBER, presenter.successData.getWeather().getBonusResource());
    assertTrue(presenter.successData.getWeather().hasBonus());
  }

  /** Tests that fog does not provide a resource bonus. */
  @Test
  void fogWeatherHasNoBonus() {
    FakeWeatherDataAccessObject weatherDataAccessObject = new FakeWeatherDataAccessObject(45);
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();

    WeatherInteractor interactor = new WeatherInteractor(weatherDataAccessObject, presenter);

    interactor.execute(new WeatherInputData("London"));

    assertNotNull(presenter.successData);
    assertEquals(WeatherType.FOG, presenter.successData.getWeather().getCondition());
    assertFalse(presenter.successData.getWeather().hasBonus());
    assertEquals(1, presenter.successData.getWeather().getMultiplier());
  }

  /** Provides predefined weather codes without accessing the network. */
  private static class FakeWeatherDataAccessObject implements WeatherDataAccessInterface {

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
    public int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException {

      lastRequestedCity = city;
      callCount++;

      if (weatherCode == null) {
        throw new WeatherUnavailableException("Weather service unavailable");
      }

      return weatherCode;
    }
  }

  /** Records the interactor's success and failure responses. */
  private static class FakeWeatherPresenter implements WeatherOutputBoundary {

    private WeatherOutputData successData;
    private String errorMessage;

    @Override
    public void prepareSuccessView(WeatherOutputData outputData) {
      successData = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}
