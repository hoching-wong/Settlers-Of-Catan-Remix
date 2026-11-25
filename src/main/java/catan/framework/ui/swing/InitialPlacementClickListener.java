package catan.framework.ui.swing;

import catan.interfaceadapter.controller.PlaceInitialController;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import java.util.Objects;

/**
 * Passes board clicks to the initial placement use case during setup. A corner click counts
 * only while a settlement is expected and an edge click only while a road is expected, as the
 * board view model says; outside setup every click is left to the build view.
 */
public class InitialPlacementClickListener implements BoardClickListener {
    private final BoardViewModel viewModel;
    private final PlaceInitialController controller;

    public InitialPlacementClickListener(BoardViewModel viewModel, PlaceInitialController controller) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.controller = Objects.requireNonNull(controller, "controller");
    }

    @Override
    public void vertexClicked(int vertexId) {
        if (BoardState.VERTEX.equals(viewModel.getState().getSetupTarget())) {
            controller.place(vertexId);
        }
    }

    @Override
    public void edgeClicked(int edgeId) {
        if (BoardState.EDGE.equals(viewModel.getState().getSetupTarget())) {
            controller.place(edgeId);
        }
    }
}
