package catan.framework.ui.swing;

import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;
import javax.swing.JComboBox;
import javax.swing.JPanel;

/**
 * Displays a dropdown list for selecting a city before starting the game.
 */
public class WeatherSelectionPanel extends JPanel {

  private final JComboBox<String> cityDropdown = new JComboBox<>();

  /**
   * Creates an embedded weather selection panel for the start menu.
   * The parent view handles starting the game.
   *
   * @param cityNames the available city names
   */
  public WeatherSelectionPanel(List<String> cityNames) {
    setLayout(new GridLayout(1, 1));
    initializeDropdown(cityNames);
    add(cityDropdown);
  }

  /**
   * Initializes the dropdown with the available cities.
   *
   * @param cityNames the city names to display
   */
  private void initializeDropdown(List<String> cityNames) {
    cityDropdown.setName("city");

    for (String cityName : Objects.requireNonNull(
            cityNames, "cityNames")) {
      cityDropdown.addItem(cityName);
    }

    cityDropdown.setSelectedIndex(-1);
  }

  /**
   * Returns the currently selected city.
   *
   * @return the selected city name, or null if none is selected
   */
  public String getSelectedCity() {
    return (String) cityDropdown.getSelectedItem();
  }

  /**
   * Clears the city selection.
   */
  public void clearSelection() {
    cityDropdown.setSelectedIndex(-1);
  }
}
