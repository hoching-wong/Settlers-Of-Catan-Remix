package catan.usecase.weather;

/**
 * Defines the input boundary for the weather use case.
 */
public interface WeatherInputBoundary {

    /**
     * Retrieves weather information for the selected city.
     *
     * @param inputData the weather use case input data
     */
    void execute(WeatherInputData inputData);
}

