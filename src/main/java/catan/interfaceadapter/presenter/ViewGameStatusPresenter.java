package catan.interfaceadapter.presenter;

import catan.entity.ResourceType;
import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.usecase.viewgamestatus.ViewGameStatusOutputBoundary;
import catan.usecase.viewgamestatus.ViewGameStatusOutputData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Converts game status output data into displayable information. */
public class ViewGameStatusPresenter implements ViewGameStatusOutputBoundary {
  private final GameStatusViewModel viewModel;

  /**
   * Creates a presenter for the game status view.
   *
   * @param viewModel the view model to update
   */
  public ViewGameStatusPresenter(GameStatusViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);
  }

  @Override
  public void prepareSuccessView(ViewGameStatusOutputData outputData) {
    List<GameStatusViewModel.PlayerStatus> players = new ArrayList<>();

    for (ViewGameStatusOutputData.PlayerStatus player : outputData.players()) {
      Map<String, Integer> resources = new HashMap<>();

      for (Map.Entry<ResourceType, Integer> entry : player.resources().entrySet()) {
        resources.put(entry.getKey().name(), entry.getValue());
      }

      players.add(
              new GameStatusViewModel.PlayerStatus(
                      player.playerName(), player.victoryPoints(), resources));
    }

    String weatherText = "Weather: None";
    String bonusText = "Bonus: None";

    if (outputData.weatherCondition() != null) {
      weatherText = "Weather: " + Text.title(outputData.weatherCondition());
    }

    if (outputData.bonusResource() != null) {
      bonusText =
              "Bonus: "
                      + Text.title(outputData.bonusResource())
                      + " x"
                      + outputData.multiplier();
    }

    viewModel.setStatus(players, outputData.currentPlayerName(), weatherText, bonusText);
  }

  @Override
  public void prepareFailView(String errorMessage) {
    viewModel.setError(errorMessage);
  }
}
