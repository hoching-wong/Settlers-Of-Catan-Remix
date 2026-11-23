package catan.interfaceadapters.longestroad;

import java.util.Objects;

/**
 * Prepares Longest Road information for the view.
 */
public class LongestRoadPresenter {

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
     * Presents the current Longest Road holder.
     *
     * @param holderName the holder's name, or null if nobody qualifies
     * @param roadLength the holder's longest road length
     */
    public void present(String holderName, int roadLength) {
        viewModel.updateState(holderName, roadLength);
    }
}

