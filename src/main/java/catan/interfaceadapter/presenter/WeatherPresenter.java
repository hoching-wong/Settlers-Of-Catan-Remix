package catan.interfaceadapter.presenter;

import catan.entity.WeatherData;
import catan.interfaceadapter.viewmodel.WeatherViewModel;
import catan.usecase.weather.WeatherOutputBoundary;
import catan.usecase.weather.WeatherOutputData;
import java.util.Objects;

/** Converts weather output data into information for the GUI. */
public class WeatherPresenter implements WeatherOutputBoundary {

  private final WeatherViewModel viewModel;

  /**
   * Constructs the weather presenter.
   *
   * @param viewModel the weather view model
   */
  public WeatherPresenter(WeatherViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);
  }

  /**
   * Updates the view model with weather information.
   *
   * @param outputData the weather output data
   */
  @Override
  public void prepareSuccessView(WeatherOutputData outputData) {
    WeatherData weather = outputData.getWeather();

    String weatherCondition = "Unavailable";

    if (weather.getCondition() != null) {
      weatherCondition =
          switch (weather.getCondition()) {
            case SUNNY -> "Sunny";
            case CLOUDY -> "Cloudy";
            case RAINY -> "Rainy";
            case SNOWY -> "Snowy";
            case STORMY -> "Stormy";
            case FOG -> "Fog";
          };
    }

    String bonusDescription = "No weather bonus";

    if (weather.hasBonus()) {
      String resourceName =
          switch (weather.getBonusResource()) {
            case LUMBER -> "Lumber";
            case BRICK -> "Brick";
            case GRAIN -> "Grain";
            case WOOL -> "Wool";
            case ORE -> "Ore";
          };

      bonusDescription = resourceName + " ×" + weather.getMultiplier();
    }

    viewModel.setWeatherInformation(
        outputData.getCity().getName(), weatherCondition, bonusDescription);
  }

  /**
   * Updates the view model with an error message.
   *
   * @param errorMessage the error message
   */
  @Override
  public void prepareFailView(String errorMessage) {
    viewModel.setErrorMessage(errorMessage);
  }
}
