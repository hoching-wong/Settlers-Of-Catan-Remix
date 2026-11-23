package presenter;

import static org.junit.jupiter.api.Assertions.*;

import entity.BoardFactory;
import interfaceadapter.viewmodel.BoardPoint;
import interfaceadapter.viewmodel.BoardState;
import interfaceadapter.viewmodel.BoardViewModel;
import interfaceadapter.viewmodel.EdgeView;
import interfaceadapter.viewmodel.TileView;
import interfaceadapter.viewmodel.VertexView;
import usecase.boardstate.BoardSnapshot;
import usecase.build.BuildOutputData;
import usecase.generateboard.GenerateBoardOutputData;
import usecase.selectstructure.SelectStructureOutputData;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BoardPresentersTest {
    private final BoardViewModel vm = new BoardViewModel();
    private final BoardSnapshot snapshot = BoardSnapshot.of(BoardFactory.createStandardBoard(new Random(3)), List.of());
    private int fired;

    BoardPresentersTest() {
        vm.addPropertyChangeListener(e -> fired++);
    }

    @Test
    void geometryMatchesTheFactory() {
        BoardPoint centre = BoardGeometry.centre(0, 0);
        assertEquals(0, centre.x(), 1e-9);
        assertEquals(0, centre.y(), 1e-9);
        for (int i = 0; i < 6; i++) {
            BoardPoint c = BoardGeometry.corner(0, 0, i);
            assertEquals(1.0, Math.hypot(c.x(), c.y()), 1e-9);
        }
        assertEquals(1, BoardGeometry.pips(2));
        assertEquals(5, BoardGeometry.pips(8));
        assertEquals(0, BoardGeometry.pips(0));
    }

    @Test
    void sharedCornersGetOnePositionAndEdgesAreOneUnitLong() {
        BoardState state = new BoardState();
        BoardStateMapper.applySnapshot(state, snapshot);
        assertEquals(19, state.getTiles().size());
        assertEquals(54, state.getVertices().size());
        for (EdgeView e : state.getEdges()) {
            assertNotNull(e.from());
            assertEquals(1.0, Math.hypot(e.to().x() - e.from().x(), e.to().y() - e.from().y()), 1e-9);
        }
        // every tile's corner list lands on the vertex positions
        for (int t = 0; t < 19; t++) {
            TileView tile = state.getTiles().get(t);
            for (int i = 0; i < 6; i++) {
                VertexView v = state.getVertices().get(snapshot.tiles().get(t).cornerVertexIds().get(i));
                assertEquals(tile.corners().get(i).x(), v.position().x(), 1e-9);
                assertEquals(tile.corners().get(i).y(), v.position().y(), 1e-9);
            }
        }
    }

    @Test
    void generatePresenterFillsTheBoard() {
        new GenerateBoardPresenter(vm).prepareSuccessView(new GenerateBoardOutputData(snapshot));
        assertEquals(19, vm.getState().getTiles().size());
        TileView any = vm.getState().getTiles().get(0);
        assertEquals(any.numberToken() == 6 || any.numberToken() == 8, any.red());
        assertFalse(vm.getState().isError());
        assertEquals(1, fired);
    }

    @Test
    void selectPresenterHighlightsAndFailClears() {
        SelectStructurePresenter p = new SelectStructurePresenter(vm);
        p.prepareSuccessView(new SelectStructureOutputData("ROAD", "EDGE", List.of(3, 4), "Ann"));
        assertEquals("ROAD", vm.getState().getSelectedStructure());
        assertEquals(BoardState.EDGE, vm.getState().getTargetKind());
        assertEquals(2, vm.getState().getHighlightedEdgeIds().size());
        assertTrue(vm.getState().getHighlightedVertexIds().isEmpty());
        assertTrue(vm.getState().getMessage().contains("edge"));

        p.prepareFailView("Roll the dice before building.");
        assertNull(vm.getState().getSelectedStructure());
        assertTrue(vm.getState().getHighlightedEdgeIds().isEmpty());
        assertTrue(vm.getState().isError());
    }

    @Test
    void buildPresenterKeepsSelectionOnFailAndClearsOnSuccess() {
        vm.getState().select("SETTLEMENT", BoardState.VERTEX, java.util.Set.of(1));
        BuildPresenter p = new BuildPresenter(vm);
        p.prepareFailView("nope");
        assertEquals("SETTLEMENT", vm.getState().getSelectedStructure());
        assertTrue(vm.getState().isError());

        p.prepareSuccessView(new BuildOutputData("SETTLEMENT", 1, "Ann", 10, Map.of("ORE", 0), "Ann", true, "Ann",
                snapshot));
        assertNull(vm.getState().getSelectedStructure());
        assertFalse(vm.getState().isError());
        assertTrue(vm.getState().getMessage().contains("Longest Road"));
        assertTrue(vm.getState().getMessage().contains("wins"));
    }
}
