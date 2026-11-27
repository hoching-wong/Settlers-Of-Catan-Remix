package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.usecase.build.BuildOutputBoundary;
import catan.usecase.build.BuildOutputData;
import catan.usecase.longestroad.LongestRoadOutputBoundary;
import catan.usecase.longestroad.LongestRoadOutputData;
import java.util.Locale;

/** Redraws the board after a build, or shows why the build was rejected. */
public class BuildPresenter implements BuildOutputBoundary {
    private final BoardViewModel viewModel;
    private final LongestRoadOutputBoundary longestRoadPresenter;

    public BuildPresenter(BoardViewModel viewModel,
                          LongestRoadOutputBoundary longestRoadPresenter) {
        this.viewModel = viewModel;
        this.longestRoadPresenter = longestRoadPresenter;
    }

    @Override
    public void prepareSuccessView(BuildOutputData outputData) {
        BoardState state = viewModel.getState();
        BoardStateMapper.applySnapshot(state, outputData.board());
        state.clearSelection();
        StringBuilder message = new StringBuilder()
                .append(outputData.playerName()).append(" built a ")
                .append(outputData.structure().toLowerCase(Locale.ROOT))
                .append(" and has ").append(outputData.victoryPoints()).append(" points.");
        if (outputData.playerName().equals(outputData.longestRoadHolder())) {
            message.append(" They hold the Longest Road.");
        }
        if (outputData.gameOver()) {
            message.append(" ").append(outputData.winnerName()).append(" wins the game!");
        }
        state.setMessage(message.toString(), false);

        longestRoadPresenter.present(new LongestRoadOutputData(
                outputData.longestRoadHolder(),
                outputData.longestRoadLength()));

        viewModel.firePropertyChanged();
    }

    /** The selection and highlights stay, so the player can simply click somewhere else. */
    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.getState().setMessage(errorMessage, true);
        viewModel.firePropertyChanged();
    }
}
