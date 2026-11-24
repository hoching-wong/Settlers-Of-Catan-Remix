package catan.interfaceadapter.controller;

import catan.usecase.weather.WeatherInputBoundary;
import catan.usecase.weather.WeatherInputData;
import java.util.Objects;

/**
 * Handles city selection requests from the weather GUI.
 */
public class WeatherController {

    private final WeatherInputBoundary weatherInteractor;

    /**
     * Constructs the weather controller.
     *
     * @param weatherInteractor the weather input boundary
     */
    public WeatherController(WeatherInputBoundary weatherInteractor) {
        this.weatherInteractor =
                Objects.requireNonNull(weatherInteractor);
    }

    /**
     * Requests weather information for the selected city.
     *
     * @param cityName the name of the selected city
     */
    public void selectCity(String cityName) {
        WeatherInputData inputData = new WeatherInputData(cityName);
        weatherInteractor.execute(inputData);
    }
}

