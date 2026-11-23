
package catan.usecase.weather;

/**
 * Defines the output boundary for the weather use case.
 */
public interface WeatherOutputBoundary {

    /**
     * Presents the selected city's weather data.
     *
     * @param outputData the weather use case output data
     */
    void prepareSuccessView(WeatherOutputData outputData);

    /**
     * Presents an error when city selection is invalid.
     *
     * @param errorMessage the error message to display
     */
    void prepareFailView(String errorMessage);
}

