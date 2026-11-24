package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.usecase.generateboard.GenerateBoardOutputBoundary;
import catan.usecase.generateboard.GenerateBoardOutputData;

/** Shows a newly generated board. */
public class GenerateBoardPresenter implements GenerateBoardOutputBoundary {
    private final BoardViewModel viewModel;

    public GenerateBoardPresenter(BoardViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(GenerateBoardOutputData outputData) {
        BoardState state = viewModel.getState();
        BoardStateMapper.applySnapshot(state, outputData.board());
        state.clearSelection();
        state.setMessage("A new board has been generated.", false);
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.getState().setMessage(errorMessage, true);
        viewModel.firePropertyChanged();
    }
}
