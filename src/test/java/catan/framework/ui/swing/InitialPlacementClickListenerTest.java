package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.controller.PlaceInitialController;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.usecase.placeinitial.PlaceInitialInputData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InitialPlacementClickListenerTest {
    private final BoardViewModel viewModel = new BoardViewModel();
    private final List<Integer> placed = new ArrayList<>();
    private final PlaceInitialController controller =
            new PlaceInitialController((PlaceInitialInputData data) -> placed.add(data.locationId()));
    private final InitialPlacementClickListener listener =
            new InitialPlacementClickListener(viewModel, controller);

    @Test
    void whileASettlementIsExpectedACornerClickPlacesIt() {
        viewModel.getState().setSetupTarget(BoardState.VERTEX);
        listener.vertexClicked(12);
        assertEquals(List.of(12), placed);
    }

    @Test
    void whileASettlementIsExpectedAnEdgeClickIsIgnored() {
        viewModel.getState().setSetupTarget(BoardState.VERTEX);
        listener.edgeClicked(40);
        assertTrue(placed.isEmpty(), "an edge id must never be taken for a vertex id");
    }

    @Test
    void whileARoadIsExpectedAnEdgeClickPlacesIt() {
        viewModel.getState().setSetupTarget(BoardState.EDGE);
        listener.edgeClicked(40);
        assertEquals(List.of(40), placed);
    }

    @Test
    void whileARoadIsExpectedACornerClickIsIgnored() {
        viewModel.getState().setSetupTarget(BoardState.EDGE);
        listener.vertexClicked(12);
        assertTrue(placed.isEmpty());
    }

    @Test
    void outsideSetupNoClickPlacesAnything() {
        listener.vertexClicked(12);
        listener.edgeClicked(40);
        assertTrue(placed.isEmpty(), "regular play is the build view's job");
    }

    @Test
    void readsTheTargetAtTheTimeOfTheClick() {
        viewModel.getState().setSetupTarget(BoardState.VERTEX);
        listener.vertexClicked(1);
        viewModel.getState().setSetupTarget(BoardState.EDGE);
        listener.edgeClicked(2);
        viewModel.getState().setSetupTarget(null);
        listener.vertexClicked(3);
        assertEquals(List.of(1, 2), placed);
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new InitialPlacementClickListener(null, controller));
        assertThrows(NullPointerException.class, () -> new InitialPlacementClickListener(viewModel, null));
    }
}
