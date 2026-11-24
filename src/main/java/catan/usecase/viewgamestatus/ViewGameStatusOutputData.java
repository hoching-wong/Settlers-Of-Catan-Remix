package catan.usecase.viewgamestatus;

import catan.entity.ResourceType;
import catan.entity.WeatherType;
import java.util.List;
import java.util.Map;

/** Contains the current game status to be displayed by the presenter. */
public record ViewGameStatusOutputData(
    List<PlayerStatus> players,
    String currentPlayerName,
    WeatherType weatherCondition,
    ResourceType bonusResource,
    int multiplier) {

  /** Contains the status of one player. */
  public record PlayerStatus(
      String playerName, int victoryPoints, Map<ResourceType, Integer> resources) {}
}
