package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.interfaceadapter.controller.WeatherController;
import catan.interfaceadapter.viewmodel.WeatherViewModel;
import catan.usecase.weather.WeatherInputBoundary;
import java.awt.Component;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Tests the WeatherSelectionPanel. */
class WeatherSelectionPanelTest {

  /**
   * Tests that the dropdown contains six predefined cities.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void dropdownContainsSixCities() throws Exception {
    WeatherSelectionPanel panel = createPanel(inputData -> {}, new WeatherViewModel());

    SwingUtilities.invokeAndWait(() -> {
      JComboBox<?> dropdown = findDropdown(panel);

      assertEquals(6, dropdown.getItemCount());
      assertEquals("Toronto", dropdown.getItemAt(0));
      assertEquals("London", dropdown.getItemAt(1));
      assertEquals("Tokyo", dropdown.getItemAt(2));
      assertEquals("New York", dropdown.getItemAt(3));
      assertEquals("Paris", dropdown.getItemAt(4));
      assertEquals("Sydney", dropdown.getItemAt(5));
      assertEquals(-1, dropdown.getSelectedIndex());
    });
  }

  /**
   * Tests that a message appears when no city is selected.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void noCitySelectedShowsMessage() throws Exception {
    WeatherSelectionPanel panel = createPanel(inputData -> {}, new WeatherViewModel());

    SwingUtilities.invokeAndWait(() -> {
      findStartButton(panel).doClick();

      assertTrue(hasLabel(panel, "Please select a city."));
    });
  }

  /**
   * Tests that clicking Start Game passes the selected city to the controller.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void startButtonPassesSelectedCity() throws Exception {
    AtomicReference<String> receivedCity = new AtomicReference<>();
    CountDownLatch requestReceived = new CountDownLatch(1);

    WeatherInputBoundary inputBoundary =
        inputData -> {
          receivedCity.set(inputData.getCityName());
          requestReceived.countDown();
        };

    WeatherSelectionPanel panel = createPanel(inputBoundary, new WeatherViewModel());

    SwingUtilities.invokeAndWait(() -> {
      findDropdown(panel).setSelectedItem("Paris");
      findStartButton(panel).doClick();
    });

    assertTrue(requestReceived.await(5, TimeUnit.SECONDS));
    assertEquals("Paris", receivedCity.get());
  }

  /**
   * Tests that weather information updates the labels.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void weatherUpdateChangesLabels() throws Exception {
    WeatherViewModel viewModel = new WeatherViewModel();

    WeatherSelectionPanel panel = createPanel(inputData -> {}, viewModel);

    SwingUtilities.invokeAndWait(() -> {
      viewModel.setWeatherInformation("Paris", "Stormy", "Ore ×2");
    });

    SwingUtilities.invokeAndWait(() -> {
      assertTrue(hasLabel(panel, "City: Paris"));
      assertTrue(hasLabel(panel, "Weather: Stormy"));
      assertTrue(hasLabel(panel, "Bonus: Ore ×2"));
    });
  }

  /**
   * Creates a weather selection panel on the Swing event thread.
   *
   * @param inputBoundary the weather input boundary
   * @param viewModel the weather view model
   * @return the created weather selection panel
   * @throws Exception if a Swing operation fails
   */
  private static WeatherSelectionPanel createPanel(
      WeatherInputBoundary inputBoundary, WeatherViewModel viewModel) throws Exception {

    AtomicReference<WeatherSelectionPanel> panel = new AtomicReference<>();

    SwingUtilities.invokeAndWait(() -> {
      WeatherController controller = new WeatherController(inputBoundary);

      panel.set(new WeatherSelectionPanel(controller, viewModel));
    });

    return panel.get();
  }

  /**
   * Finds the city dropdown in the panel.
   *
   * @param panel the weather selection panel
   * @return the city dropdown
   */
  private static JComboBox<?> findDropdown(WeatherSelectionPanel panel) {

    for (Component component : panel.getComponents()) {
      if (component instanceof JComboBox<?> dropdown) {
        return dropdown;
      }
    }

    throw new AssertionError("City dropdown not found");
  }

  /**
   * Finds the Start Game button in the panel.
   *
   * @param panel the weather selection panel
   * @return the Start Game button
   */
  private static JButton findStartButton(WeatherSelectionPanel panel) {

    for (Component component : panel.getComponents()) {
      if (component instanceof JButton button) {
        return button;
      }
    }

    throw new AssertionError("Start Game button not found");
  }

  /**
   * Checks whether a label displays the expected text.
   *
   * @param panel the weather selection panel
   * @param expectedText the expected label text
   * @return true if the label exists
   */
  private static boolean hasLabel(WeatherSelectionPanel panel, String expectedText) {

    for (Component component : panel.getComponents()) {
      if (component instanceof JLabel label && expectedText.equals(label.getText())) {
        return true;
      }
    }

    return false;
  }
}
