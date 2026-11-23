package catan.interfaceadapters.weather;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import catan.usecase.weather.WeatherInputBoundary;
import catan.usecase.weather.WeatherInputData;
import org.junit.jupiter.api.Test;

/** Tests for WeatherController. */
class WeatherControllerTest {

  @Test
  void selectCityPassesCityNameToInteractor() {
    FakeWeatherInteractor fakeInteractor = new FakeWeatherInteractor();
    WeatherController controller = new WeatherController(fakeInteractor);

    controller.selectCity("Paris");

    assertEquals("Paris", fakeInteractor.receivedInputData.getCityName());
  }

  @Test
  void selectCityPassesNullForValidation() {
    FakeWeatherInteractor fakeInteractor = new FakeWeatherInteractor();
    WeatherController controller = new WeatherController(fakeInteractor);

    controller.selectCity(null);

    assertNull(fakeInteractor.receivedInputData.getCityName());
  }

  @Test
  void constructorRejectsNullInteractor() {
    assertThrows(NullPointerException.class, () -> new WeatherController(null));
  }

  /** Records the input received from WeatherController. */
  private static class FakeWeatherInteractor implements WeatherInputBoundary {

    private WeatherInputData receivedInputData;

    @Override
    public void execute(WeatherInputData inputData) {
      this.receivedInputData = inputData;
    }
  }
}
