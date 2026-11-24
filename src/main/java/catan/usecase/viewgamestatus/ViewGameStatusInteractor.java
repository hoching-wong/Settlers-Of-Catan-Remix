package catan.usecase.viewgamestatus;

import catan.entity.Game;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Retrieves the current game status for display. */
public class ViewGameStatusInteractor implements ViewGameStatusInputBoundary {
  private final GameRepository gameRepository;
  private final ViewGameStatusOutputBoundary presenter;

  /**
   * Creates an interactor with the required dependencies.
   *
   * @param gameRepository the repository containing the current game
   * @param presenter the output boundary for displaying game status
   */
  public ViewGameStatusInteractor(
      GameRepository gameRepository, ViewGameStatusOutputBoundary presenter) {
    this.gameRepository = Objects.requireNonNull(gameRepository);
    this.presenter = Objects.requireNonNull(presenter);
  }

  @Override
  public void viewGameStatus() {
    Game game = gameRepository.get();

    if (game == null) {
      presenter.prepareFailView("No game is in progress.");
      return;
    }

    List<ViewGameStatusOutputData.PlayerStatus> players = new ArrayList<>();

    for (Player player : game.getPlayers()) {
      Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);

      for (ResourceType type : ResourceType.values()) {
        resources.put(type, player.getResources().getResourceCount(type));
      }

      players.add(
          new ViewGameStatusOutputData.PlayerStatus(
              player.getName(), player.getVictoryPoints(), resources));
    }

    WeatherData weather = game.getWeather();

    ViewGameStatusOutputData outputData =
        new ViewGameStatusOutputData(
            players,
            game.getCurrentPlayer().getName(),
            weather.getCondition(),
            weather.getBonusResource(),
            weather.getMultiplier());

    presenter.prepareSuccessView(outputData);
  }
}
