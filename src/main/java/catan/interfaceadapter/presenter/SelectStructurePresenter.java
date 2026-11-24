package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.usecase.selectstructure.SelectStructureOutputBoundary;
import catan.usecase.selectstructure.SelectStructureOutputData;
import java.util.HashSet;
import java.util.Locale;

/** Highlights where the chosen structure can go. */
public class SelectStructurePresenter implements SelectStructureOutputBoundary {
    private final BoardViewModel viewModel;

    public SelectStructurePresenter(BoardViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(SelectStructureOutputData outputData) {
        BoardState state = viewModel.getState();
        state.select(outputData.structure(), outputData.locationKind(), new HashSet<>(outputData.validLocationIds()));
        String what = outputData.structure().toLowerCase(Locale.ROOT);
        String where = BoardState.EDGE.equals(outputData.locationKind()) ? "edge" : "corner";
        state.setMessage(outputData.playerName() + ": click a highlighted " + where + " to build a " + what + ".",
                false);
        viewModel.firePropertyChanged();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        BoardState state = viewModel.getState();
        state.clearSelection();
        state.setMessage(errorMessage, true);
        viewModel.firePropertyChanged();
    }
}
