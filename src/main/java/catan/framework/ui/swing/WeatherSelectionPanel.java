package catan.framework.ui.swing;

import catan.entity.CityLocation;
import catan.interfaceadapter.controller.WeatherController;
import catan.interfaceadapter.viewmodel.WeatherViewModel;
import java.awt.GridLayout;
import java.util.List;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 * Displays a dropdown list for selecting a city's weather.
 * Supports both standalone and embedded use.
 */
public class WeatherSelectionPanel extends JPanel {

  private final WeatherController controller;
  private final WeatherViewModel viewModel;

  private final JComboBox<String> cityDropdown = new JComboBox<>();
  private final JButton startButton = new JButton("Start Game");

  private final JLabel cityLabel = new JLabel("City: -");
  private final JLabel weatherLabel = new JLabel("Weather: -");
  private final JLabel bonusLabel = new JLabel("Bonus: -");
  private final JLabel statusLabel =
          new JLabel("Select a city to start.");

  /**
   * Creates an embedded weather selection panel for the start menu.
   * The parent view handles starting the game.
   *
   * @param cityNames the available city names
   */
  public WeatherSelectionPanel(List<String> cityNames) {
    controller = null;
    viewModel = null;

    setLayout(new GridLayout(1, 1));
    initializeDropdown(cityNames);
    add(cityDropdown);
  }

  /**
   * Constructs the standalone weather selection panel.
   *
   * @param controller the weather controller
   * @param viewModel the weather view model
   */
  public WeatherSelectionPanel(
          WeatherController controller,
          WeatherViewModel viewModel) {

    this.controller = Objects.requireNonNull(
            controller, "controller");
    this.viewModel = Objects.requireNonNull(
            viewModel, "viewModel");

    setLayout(new GridLayout(0, 1, 0, 8));

    add(new JLabel("Select a city:"));

    initializeDropdown(
            CityLocation.defaults().stream()
                    .map(CityLocation::getName)
                    .toList());

    add(cityDropdown);
    add(startButton);
    add(cityLabel);
    add(weatherLabel);
    add(bonusLabel);
    add(statusLabel);

    startButton.addActionListener(
            event -> startWeatherLookup());

    viewModel.addPropertyChangeListener(event -> {
      if (WeatherViewModel.WEATHER_UPDATED.equals(
              event.getPropertyName())) {

        SwingUtilities.invokeLater(() -> {
          cityLabel.setText(
                  "City: " + viewModel.getCityName());
          weatherLabel.setText(
                  "Weather: " + viewModel.getWeatherCondition());
          bonusLabel.setText(
                  "Bonus: " + viewModel.getBonusDescription());
          statusLabel.setText(
                  "Weather information retrieved.");
        });

      } else if (WeatherViewModel.ERROR_UPDATED.equals(
              event.getPropertyName())) {

        SwingUtilities.invokeLater(() -> {
          statusLabel.setText(viewModel.getErrorMessage());
          cityDropdown.setEnabled(true);
          startButton.setEnabled(true);
        });
      }
    });
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

  /**
   * Retrieves weather when the standalone Start Game button
   * is clicked.
   */
  private void startWeatherLookup() {
    String selectedCity = getSelectedCity();

    if (selectedCity == null) {
      statusLabel.setText("Please select a city.");
      return;
    }

    startButton.setEnabled(false);
    cityDropdown.setEnabled(false);
    statusLabel.setText("Retrieving weather...");

    SwingWorker<Void, Void> worker = new SwingWorker<>() {

      @Override
      protected Void doInBackground() {
        controller.selectCity(selectedCity);
        return null;
      }

      @Override
      protected void done() {
        try {
          get();
        } catch (InterruptedException exception) {
          Thread.currentThread().interrupt();
          statusLabel.setText(
                  "Weather request interrupted.");
          startButton.setEnabled(true);
          cityDropdown.setEnabled(true);

        } catch (java.util.concurrent.ExecutionException exception) {
          statusLabel.setText(
                  "Unable to process weather information.");
          startButton.setEnabled(true);
          cityDropdown.setEnabled(true);
        }
      }
    };

    worker.execute();
  }
}
