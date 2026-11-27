
package catan.interfaceadapter.viewmodel;

import java.util.List;
import java.util.Map;

/**
 * Stores the current game status displayed by the GUI.
 */
public class GameStatusViewModel extends ViewModel {
  public static final String STATUS = "status";
  public static final String ERROR = "error";

  private List<PlayerStatus> players = List.of();
  private String currentPlayerName = "";
  private String weatherText = "";
  private String bonusText = "";
  private String errorMessage = "";

  public List<PlayerStatus> getPlayers() {
    return players;
  }

  public String getCurrentPlayerName() {
    return currentPlayerName;
  }

  public String getWeatherText() {
    return weatherText;
  }

  public String getBonusText() {
    return bonusText;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  /**
   * Updates the current game status and notifies listeners.
   *
   * @param players the status of all players
   * @param currentPlayerName the current player's name
   * @param weatherText the current weather condition
   * @param bonusText the weather-based resource bonus
   */
  public void setStatus(
      List<PlayerStatus> players, String currentPlayerName, String weatherText, String bonusText) {
    this.players = List.copyOf(players);
    this.currentPlayerName = currentPlayerName;
    this.weatherText = weatherText;
    this.bonusText = bonusText;
    this.errorMessage = "";
    firePropertyChanged(STATUS, this.players);
  }

  /**
   * Updates the error message and notifies listeners.
   *
   * @param message the error message to display
   */
  public void setError(String message) {
    this.errorMessage = message;
    firePropertyChanged(ERROR, message);
  }

  /**
   * Status information for one player.
   *
   * @param playerName the player's name
   * @param victoryPoints the player's total victory points
   * @param resources the player's resource counts
   */
  public record PlayerStatus(String playerName, int victoryPoints, Map<String, Integer> resources) {

    /** Creates a player status with an immutable resource map. */
    public PlayerStatus {
      resources = Map.copyOf(resources);
    }
  }
}
