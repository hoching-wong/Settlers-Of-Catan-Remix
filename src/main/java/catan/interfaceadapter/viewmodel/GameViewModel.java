package catan.interfaceadapter.viewmodel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** What the game screen shows about the current turn. */
public class GameViewModel extends ViewModel {
    public static final String PLAYERS = "players";
    public static final String TURN = "turn";
    public static final String MESSAGE = "message";
    public static final String ERROR = "error";
    public static final String WEATHER = "weather";

    private String weatherText = "";
    private String bonusText = "";
    private List<String> playerNames = List.of();
    private String currentPlayerName = "";
    private String instruction = "";
    private String message = "";
    private String errorMessage = "";

    public List<String> getPlayerNames() {
        return playerNames;
    }

    /** The player whose turn it is, for the "Current turn" label. */
    public String getCurrentPlayerName() {
        return currentPlayerName;
    }

    /** What the current player should do next, for example "Place a settlement". */
    public String getInstruction() {
        return instruction;
    }

    /** A note about what just happened, or an empty string. */
    public String getMessage() {
        return message;
    }

    /** The reason the last action was refused, or an empty string. */
    public String getErrorMessage() {
        return errorMessage;
    }

    /** The weather of the game, for example "Weather in Toronto: Sunny". Fixed for the whole game. */
    public String getWeatherText() {
        return weatherText;
    }

    /** The resource bonus the weather gives, for example "Bonus: Lumber x2". */
    public String getBonusText() {
        return bonusText;
    }

    public void setWeather(String weatherText, String bonusText) {
        this.weatherText = weatherText;
        this.bonusText = bonusText;
        firePropertyChanged(WEATHER, weatherText);
    }

    /** A new game has begun. */
    public void startGame(List<String> playerNames, String firstPlayerName, String instruction) {
        this.playerNames = Collections.unmodifiableList(new ArrayList<>(playerNames));
        this.message = "";
        this.errorMessage = "";
        firePropertyChanged(PLAYERS, this.playerNames);
        setTurn(firstPlayerName, instruction);
    }

    /** Whose turn it is and what they should do. Clears the old error. */
    public void setTurn(String currentPlayerName, String instruction) {
        this.currentPlayerName = currentPlayerName;
        this.instruction = instruction;
        this.errorMessage = "";
        firePropertyChanged(TURN, currentPlayerName);
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
        firePropertyChanged(MESSAGE, this.message);
    }

    public void setError(String message) {
        this.errorMessage = message == null ? "" : message;
        firePropertyChanged(ERROR, errorMessage);
    }

    /** Back to an empty screen for the next game. */
    public void reset() {
        playerNames = List.of();
        currentPlayerName = "";
        instruction = "";
        message = "";
        errorMessage = "";
        weatherText = "";
        bonusText = "";
        firePropertyChanged(PLAYERS, playerNames);
        firePropertyChanged(TURN, currentPlayerName);
        firePropertyChanged(MESSAGE, message);
        firePropertyChanged(ERROR, errorMessage);
        firePropertyChanged(WEATHER, weatherText);
    }
}
