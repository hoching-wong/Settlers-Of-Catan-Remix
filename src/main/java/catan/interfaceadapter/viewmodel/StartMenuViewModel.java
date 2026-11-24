package catan.interfaceadapter.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What the start menu shows: the cities to choose from, an error, and the summary of the new game. */
public class StartMenuViewModel extends ViewModel {
    public static final String ERROR = "error";
    public static final String SUMMARY = "summary";

    private final List<String> cityNames;
    private String errorMessage = "";
    private List<String> playerNames = List.of();
    private String cityName = "";
    private String weatherText = "";
    private String bonusText = "";

    /** @param cityNames the cities offered in the drop-down, in display order */
    public StartMenuViewModel(List<String> cityNames) {
        this.cityNames = List.copyOf(cityNames);
    }

    public List<String> getCityNames() {
        return cityNames;
    }

    /** The message for the player, or an empty string if there is none. */
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setError(String message) {
        this.errorMessage = message == null ? "" : message;
        firePropertyChanged(ERROR, errorMessage);
    }

    public List<String> getPlayerNames() {
        return playerNames;
    }

    public String getCityName() {
        return cityName;
    }

    public String getWeatherText() {
        return weatherText;
    }

    public String getBonusText() {
        return bonusText;
    }

    /** Records the summary of the game that was just created and clears any error. */
    public void setGameStarted(List<String> playerNames, String cityName, String weatherText, String bonusText) {
        this.playerNames = Collections.unmodifiableList(new ArrayList<>(playerNames));
        this.cityName = cityName;
        this.weatherText = weatherText;
        this.bonusText = bonusText;
        this.errorMessage = "";
        firePropertyChanged(SUMMARY, this.cityName);
    }

    /** Back to an empty menu for the next game. */
    public void reset() {
        playerNames = List.of();
        cityName = "";
        weatherText = "";
        bonusText = "";
        errorMessage = "";
        firePropertyChanged(SUMMARY, cityName);
        firePropertyChanged(ERROR, errorMessage);
    }
}
