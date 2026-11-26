package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.usecase.longestroad.LongestRoadOutputBoundary;
import catan.usecase.longestroad.LongestRoadOutputData;
import java.util.Objects;

/** Prepares Longest Road information for the view. */
public class LongestRoadPresenter implements LongestRoadOutputBoundary {

  private final LongestRoadViewModel viewModel;

  /**
   * Constructs a presenter with its view model.
   *
   * @param viewModel the Longest Road view model
   */
  public LongestRoadPresenter(LongestRoadViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);
  }

  /**
   * Presents the Longest Road result.
   *
   * @param outputData the result to display
   */
  @Override
  public void present(LongestRoadOutputData outputData) {
    Objects.requireNonNull(outputData);

    viewModel.updateState(outputData.holderName(), outputData.roadLength());
  }
}
