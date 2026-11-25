package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.SetupStep;
import catan.entity.Settlement;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.boardstate.BoardSnapshot;
import catan.usecase.placeinitial.PlaceInitialOutputData;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class PlaceInitialPresenterTest {
    private static final BoardSnapshot BOARD =
            BoardSnapshot.of(BoardFactory.createStandardBoard(new Random(1)), List.of());

    private final GameViewModel game = new GameViewModel();
    private final BoardViewModel board = new BoardViewModel();
    private final PlaceInitialPresenter presenter = new PlaceInitialPresenter(game, board);

    @Test
    void afterASettlementTheSamePlayerPlacesARoad() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD, BOARD));
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a road next to your new settlement", game.getInstruction());
        assertEquals("", game.getMessage());
    }

    @Test
    void afterARoadTheNextPlayerPlacesASettlement() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 8, Map.of(),
                false, "Bob", SetupStep.SETTLEMENT, BOARD));
        assertEquals("Bob", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
    }

    @Test
    void startingResourcesAreAnnounced() {
        Map<ResourceType, Integer> gained = new EnumMap<>(ResourceType.class);
        gained.put(ResourceType.GRAIN, 2);
        gained.put(ResourceType.LUMBER, 1);
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Bob", 20, gained,
                false, "Bob", SetupStep.ROAD, BOARD));
        assertEquals("Bob received 1 Lumber, 2 Grain.", game.getMessage());
    }

    @Test
    void whenSetupEndsPlayerOneRolls() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 30, Map.of(),
                true, "Ann", null, BOARD));
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Roll the dice", game.getInstruction());
        assertEquals("Setup complete.", game.getMessage());
    }

    @Test
    void anAnnouncementOfStartingResourcesBeatsTheSetupCompleteNote() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 9,
                Map.of(ResourceType.ORE, 1), false, "Ann", SetupStep.ROAD, BOARD));
        assertEquals("Ann received 1 Ore.", game.getMessage());
    }

    @Test
    void anErrorKeepsTheTurnAndShowsTheReason() {
        game.setTurn("Ann", "Place a settlement");
        presenter.presentError("That spot is already taken.");
        assertEquals("That spot is already taken.", game.getErrorMessage());
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
    }

    @Test
    void aSuccessfulPlacementClearsTheError() {
        presenter.presentError("That spot is already taken.");
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD, BOARD));
        assertEquals("", game.getErrorMessage());
    }

    // ---- the board ----

    @Test
    void thePlacementIsDrawnOnTheBoard() {
        Board b = BoardFactory.createStandardBoard(new Random(2));
        Player ann = new Player("Ann");
        b.placeBuilding(b.getVertex(5), new Settlement(ann, b.getVertex(5)));
        b.placeRoad(b.getVertex(5).getAdjacentEdges().get(0), new Road(ann, b.getVertex(5).getAdjacentEdges().get(0)));
        int roadId = b.getVertex(5).getAdjacentEdges().get(0).getId();
        int[] fired = {0};
        board.addPropertyChangeListener(e -> fired[0]++);

        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", roadId, Map.of(),
                false, "Bob", SetupStep.SETTLEMENT, BoardSnapshot.of(b, List.of(ann))));

        BoardState state = board.getState();
        assertEquals(19, state.getTiles().size());
        assertEquals("SETTLEMENT", state.getVertices().get(5).building());
        assertEquals(0, state.getVertices().get(5).ownerIndex());
        assertEquals(0, state.getEdges().get(roadId).ownerIndex());
        assertEquals(1, fired[0]);
    }

    @Test
    void afterASettlementTheNextClickPlacesARoad() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD, BOARD));
        assertEquals(BoardState.EDGE, board.getState().getSetupTarget());
    }

    @Test
    void afterARoadTheNextClickPlacesASettlement() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 8, Map.of(),
                false, "Bob", SetupStep.SETTLEMENT, BOARD));
        assertEquals(BoardState.VERTEX, board.getState().getSetupTarget());
    }

    @Test
    void whenSetupEndsClicksNoLongerPlace() {
        board.getState().setSetupTarget(BoardState.EDGE);
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 30, Map.of(),
                true, "Ann", null, BOARD));
        assertNull(board.getState().getSetupTarget());
    }

    @Test
    void anErrorLeavesTheBoardAsItWas() {
        board.getState().setSetupTarget(BoardState.VERTEX);
        presenter.presentError("That spot is already taken.");
        assertEquals(BoardState.VERTEX, board.getState().getSetupTarget());
        assertTrue(board.getState().getTiles().isEmpty());
    }

    @Test
    void worksWithoutABoardView() {
        PlaceInitialPresenter noBoard = new PlaceInitialPresenter(game);
        noBoard.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD, BOARD));
        assertEquals("Ann", game.getCurrentPlayerName());
    }

    @Test
    void rejectsANullViewModel() {
        assertThrows(NullPointerException.class, () -> new PlaceInitialPresenter(null));
        assertThrows(NullPointerException.class, () -> new PlaceInitialPresenter(null, board));
        assertThrows(NullPointerException.class, () -> new PlaceInitialPresenter(game, null));
    }
}
