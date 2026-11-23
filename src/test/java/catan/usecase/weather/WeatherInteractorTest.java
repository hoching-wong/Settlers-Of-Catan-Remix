package catan.usecase.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.entity.CityLocation;
import catan.entity.ResourceType;
import catan.entity.WeatherType;
import java.util.OptionalInt;
import org.junit.jupiter.api.Test;

/** Tests for WeatherInteractor. */
class WeatherInteractorTest {

  @Test
  void validCityRetrievesWeather() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.of(95));
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(new WeatherInputData("Paris"));

    assertEquals("Paris", dataAccess.lastRequestedCity.getName());
    assertEquals(1, dataAccess.callCount);
    assertEquals("Paris", presenter.successData.getCity().getName());
    assertEquals(WeatherType.STORMY, presenter.successData.getWeather().getCondition());
    assertEquals(ResourceType.ORE, presenter.successData.getWeather().getBonusResource());
    assertEquals(2, presenter.successData.getWeather().getMultiplier());
    assertNull(presenter.errorMessage);
  }

  @Test
  void nullInputShowsError() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.of(0));
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(null);

    assertEquals("Please select a city before starting the game.", presenter.errorMessage);
    assertNull(presenter.successData);
    assertEquals(0, dataAccess.callCount);
  }

  @Test
  void blankCityShowsError() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.of(0));
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(new WeatherInputData(""));

    assertEquals("Please select a city before starting the game.", presenter.errorMessage);
    assertEquals(0, dataAccess.callCount);
  }

  @Test
  void unknownCityShowsError() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.of(0));
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(new WeatherInputData("Berlin"));

    assertEquals("The selected city is not available.", presenter.errorMessage);
    assertNull(presenter.successData);
    assertEquals(0, dataAccess.callCount);
  }

  @Test
  void apiFailureContinuesWithoutBonus() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.empty());
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(new WeatherInputData("Toronto"));

    assertEquals(1, dataAccess.callCount);
    assertEquals("Toronto", presenter.successData.getCity().getName());
    assertFalse(presenter.successData.getWeather().hasBonus());
    assertEquals(1, presenter.successData.getWeather().getMultiplier());
    assertNull(presenter.errorMessage);
  }

  @Test
  void recognizedWeatherProvidesBonus() {
    FakeWeatherDataAccess dataAccess = new FakeWeatherDataAccess(OptionalInt.of(0));
    FakeWeatherPresenter presenter = new FakeWeatherPresenter();
    WeatherInteractor interactor = new WeatherInteractor(dataAccess, presenter);

    interactor.execute(new WeatherInputData("Sydney"));

    assertEquals(WeatherType.SUNNY, presenter.successData.getWeather().getCondition());
    assertEquals(ResourceType.LUMBER, presenter.successData.getWeather().getBonusResource());
    assertTrue(presenter.successData.getWeather().hasBonus());
  }

  /** Returns predefined weather codes without accessing the network. */
  private static class FakeWeatherDataAccess implements WeatherDataAccessInterface {

    private final OptionalInt weatherCode;
    private CityLocation lastRequestedCity;
    private int callCount;

    FakeWeatherDataAccess(OptionalInt weatherCode) {
      this.weatherCode = weatherCode;
    }

    @Override
    public OptionalInt getWeatherCode(CityLocation city) {
      this.lastRequestedCity = city;
      this.callCount++;
      return weatherCode;
    }
  }

  /** Records the Interactor's success and failure responses. */
  private static class FakeWeatherPresenter implements WeatherOutputBoundary {

    private WeatherOutputData successData;
    private String errorMessage;

    @Override
    public void prepareSuccessView(WeatherOutputData outputData) {
      this.successData = outputData;
    }

    @Override
    public void prepareFailView(String errorMessage) {
      this.errorMessage = errorMessage;
    }
  }
}
