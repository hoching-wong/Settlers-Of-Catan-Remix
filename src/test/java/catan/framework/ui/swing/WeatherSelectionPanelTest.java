package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Component;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JComboBox;
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
    WeatherSelectionPanel panel = createPanel();

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
   * Tests that no selected city returns null.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void noCitySelectedReturnsNull() throws Exception {
    WeatherSelectionPanel panel = createPanel();

    SwingUtilities.invokeAndWait(() -> {
      assertNull(panel.getSelectedCity());
    });
  }

  /**
   * Tests that the selected city is returned correctly.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void selectedCityIsReturnedCorrectly() throws Exception {
    WeatherSelectionPanel panel = createPanel();

    SwingUtilities.invokeAndWait(() -> {
      findDropdown(panel).setSelectedItem("Paris");

      assertEquals("Paris", panel.getSelectedCity());
    });
  }

  /**
   * Tests that clearing the selection removes the selected city.
   *
   * @throws Exception if a Swing operation fails
   */
  @Test
  void clearSelectionRemovesSelectedCity() throws Exception {
    WeatherSelectionPanel panel = createPanel();

    SwingUtilities.invokeAndWait(() -> {
      findDropdown(panel).setSelectedItem("Toronto");

      assertEquals("Toronto", panel.getSelectedCity());

      panel.clearSelection();

      assertNull(panel.getSelectedCity());
      assertEquals(-1, findDropdown(panel).getSelectedIndex());
    });
  }

  /**
   * Creates a weather selection panel on the Swing event thread.
   *
   * @return the created weather selection panel
   * @throws Exception if a Swing operation fails
   */
  private static WeatherSelectionPanel createPanel() throws Exception {

    AtomicReference<WeatherSelectionPanel> panel =
            new AtomicReference<>();

    SwingUtilities.invokeAndWait(() -> {
      panel.set(new WeatherSelectionPanel(List.of(
              "Toronto", "London", "Tokyo",
              "New York", "Paris", "Sydney")));
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
}
