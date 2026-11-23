package catan.interfaceadapters.weather;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Stores weather information displayed by the GUI.
 */
public class WeatherViewModel {

    public static final String WEATHER_UPDATED = "weatherUpdated";
    public static final String ERROR_UPDATED = "errorUpdated";

    private final PropertyChangeSupport support =
            new PropertyChangeSupport(this);

    private String cityName = "";
    private String weatherCondition = "";
    private String bonusDescription = "";
    private String errorMessage = "";

    /**
     * Updates the weather information displayed by the GUI.
     *
     * @param cityName the selected city
     * @param weatherCondition the weather description
     * @param bonusDescription the resource production bonus
     */
    public void setWeatherInformation(
            String cityName,
            String weatherCondition,
            String bonusDescription) {
        this.cityName = cityName;
        this.weatherCondition = weatherCondition;
        this.bonusDescription = bonusDescription;
        this.errorMessage = "";

        support.firePropertyChange(WEATHER_UPDATED, null, this);
    }

    /**
     * Updates the error message displayed by the GUI.
     *
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        support.firePropertyChange(ERROR_UPDATED, null, this);
    }

    public String getCityName() {
        return cityName;
    }

    public String getWeatherCondition() {
        return weatherCondition;
    }

    public String getBonusDescription() {
        return bonusDescription;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Registers a listener for changes to the ViewModel.
     *
     * @param listener the listener to register
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a registered listener.
     *
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}

