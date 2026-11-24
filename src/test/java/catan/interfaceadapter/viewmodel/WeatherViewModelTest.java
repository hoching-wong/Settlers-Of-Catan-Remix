package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

/** Tests for WeatherViewModel. */
class WeatherViewModelTest {

  @Test
  void initialValuesAreEmpty() {
    WeatherViewModel viewModel = new WeatherViewModel();

    assertEquals("", viewModel.getCityName());
    assertEquals("", viewModel.getWeatherCondition());
    assertEquals("", viewModel.getBonusDescription());
    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void setWeatherInformationUpdatesValues() {
    WeatherViewModel viewModel = new WeatherViewModel();

    viewModel.setWeatherInformation("Paris", "Stormy", "Ore ×2");

    assertEquals("Paris", viewModel.getCityName());
    assertEquals("Stormy", viewModel.getWeatherCondition());
    assertEquals("Ore ×2", viewModel.getBonusDescription());
    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void successfulUpdateClearsPreviousError() {
    WeatherViewModel viewModel = new WeatherViewModel();

    viewModel.setErrorMessage("Please select a city.");
    viewModel.setWeatherInformation("Toronto", "Sunny", "Lumber ×2");

    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void setErrorMessageUpdatesValue() {
    WeatherViewModel viewModel = new WeatherViewModel();

    viewModel.setErrorMessage("Please select a city.");

    assertEquals("Please select a city.", viewModel.getErrorMessage());
  }

  @Test
  void weatherUpdateNotifiesListener() {
    WeatherViewModel viewModel = new WeatherViewModel();
    AtomicReference<PropertyChangeEvent> receivedEvent = new AtomicReference<>();

    viewModel.addPropertyChangeListener(receivedEvent::set);

    viewModel.setWeatherInformation("Paris", "Stormy", "Ore ×2");

    assertNotNull(receivedEvent.get());
    assertEquals(WeatherViewModel.WEATHER_UPDATED, receivedEvent.get().getPropertyName());
    assertSame(viewModel, receivedEvent.get().getSource());
  }

  @Test
  void errorUpdateNotifiesListener() {
    WeatherViewModel viewModel = new WeatherViewModel();
    List<String> eventNames = new ArrayList<>();

    viewModel.addPropertyChangeListener(event -> eventNames.add(event.getPropertyName()));

    viewModel.setErrorMessage("Invalid city.");

    assertEquals(List.of(WeatherViewModel.ERROR_UPDATED), eventNames);
  }

  @Test
  void removedListenerIsNotNotified() {
    WeatherViewModel viewModel = new WeatherViewModel();
    List<String> eventNames = new ArrayList<>();

    PropertyChangeListener listener = event -> eventNames.add(event.getPropertyName());

    viewModel.addPropertyChangeListener(listener);
    viewModel.removePropertyChangeListener(listener);

    viewModel.setWeatherInformation("Sydney", "Sunny", "Lumber ×2");

    assertEquals(0, eventNames.size());
  }
}
