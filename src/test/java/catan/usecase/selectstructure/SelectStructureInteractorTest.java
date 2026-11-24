package catan.usecase.selectstructure;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Building;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.StructureType;
import catan.usecase.FakeGameRepository;
import catan.usecase.TestGames;
import catan.usecase.build.placement.RoadPlacement;
import java.util.List;
import org.junit.jupiter.api.Test;

class SelectStructureInteractorTest {
    private final FakeGameRepository repo = new FakeGameRepository();
    private final Recorder presenter = new Recorder();
    private final SelectStructureInteractor interactor = new SelectStructureInteractor(repo, presenter);

    private static class Recorder implements SelectStructureOutputBoundary {
        SelectStructureOutputData success;
        String error;

        @Override
        public void prepareSuccessView(SelectStructureOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }

    private Game ready() {
        Game game = TestGames.playingGame(6, "Ann", "Bob", "Cy");
        game.markRolled();
        TestGames.give(game.getCurrentPlayer(), 5);
        repo.game = game;
        return game;
    }

    @Test
    void roadHighlightsExactlyTheValidEdges() {
        Game game = ready();
        interactor.execute(new SelectStructureInputData(StructureType.ROAD));
        assertNull(presenter.error);
        assertEquals("EDGE", presenter.success.locationKind());
        assertEquals("ROAD", presenter.success.structure());
        List<Integer> expected = new RoadPlacement().validLocations(game.getBoard(), game.getCurrentPlayer());
        assertEquals(expected, presenter.success.validLocationIds());
        assertFalse(expected.isEmpty());
        for (int id : presenter.success.validLocationIds()) {
            assertTrue(game.getBoard().isValidRoadLocation(game.getBoard().getEdge(id), game.getCurrentPlayer(), null));
        }
    }

    @Test
    void cityHighlightsOwnSettlements() {
        Game game = ready();
        Player ann = game.getCurrentPlayer();
        interactor.execute(new SelectStructureInputData(StructureType.CITY));
        assertEquals("VERTEX", presenter.success.locationKind());
        List<Integer> own = ann.getBuildings().stream().map(Building::getLocation).map(v -> v.getId()).sorted().toList();
        assertEquals(own, presenter.success.validLocationIds());
    }

    @Test
    void noSettlementSpotRightAfterSetup() {
        ready();
        interactor.execute(new SelectStructureInputData(StructureType.SETTLEMENT));
        assertEquals("There is nowhere you can build a settlement right now.", presenter.error);
    }

    @Test
    void rulesRunBeforeListing() {
        Game game = TestGames.playingGame(6, "Ann", "Bob");
        repo.game = game;
        interactor.execute(new SelectStructureInputData(StructureType.ROAD));
        assertEquals("Roll the dice before building.", presenter.error);
        assertNull(presenter.success);
    }
}
