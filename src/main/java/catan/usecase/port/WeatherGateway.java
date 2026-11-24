package catan.usecase.port;

import catan.entity.CityLocation;

/** Retrieves the current weather for a city. Implemented in the framework layer. */
public interface WeatherGateway {

    /**
     * The current WMO weather code at the city's coordinates.
     *
     * @throws WeatherUnavailableException if the request fails for any reason. Callers carry on
     *     without a weather bonus.
     */
    int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException;
}
