package usecase.generateboard;

import static org.junit.jupiter.api.Assertions.*;

import entity.BalancedLayoutStrategy;
import entity.RandomLayoutStrategy;
import entity.Tile;
import usecase.FakeGameRepository;
import usecase.TestGames;
import usecase.boardstate.BoardSnapshot;
import org.junit.jupiter.api.Test;

class GenerateBoardInteractorTest {
    private final FakeGameRepository repo = new FakeGameRepository();

    private static class RecordingPresenter implements GenerateBoardOutputBoundary {
        GenerateBoardOutputData success;
        String error;

        @Override
        public void prepareSuccessView(GenerateBoardOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }

    @Test
    void generatesAndSavesABoardAndShowsIt() {
        RecordingPresenter presenter = new RecordingPresenter();
        new GenerateBoardInteractor(repo, presenter, new BalancedLayoutStrategy())
                .execute(GenerateBoardInputData.random());
        assertNotNull(repo.board);
        assertNull(presenter.error);
        BoardSnapshot snap = presenter.success.board();
        assertEquals(19, snap.tiles().size());
        assertEquals(54, snap.vertices().size());
        assertEquals(72, snap.edges().size());
        for (int i = 0; i < 19; i++) {
            Tile t = repo.board.getTile(i);
            assertEquals(t.getTerrain().name(), snap.tiles().get(i).terrain());
            assertEquals(t.getNumberToken(), snap.tiles().get(i).numberToken());
        }
        assertTrue(snap.vertices().stream().allMatch(v -> v.ownerIndex() == BoardSnapshot.NO_OWNER));
    }

    @Test
    void sameSeedGivesSameBoard() {
        RecordingPresenter a = new RecordingPresenter();
        RecordingPresenter b = new RecordingPresenter();
        new GenerateBoardInteractor(repo, a, new RandomLayoutStrategy()).execute(new GenerateBoardInputData(12L));
        new GenerateBoardInteractor(repo, b, new RandomLayoutStrategy()).execute(new GenerateBoardInputData(12L));
        assertEquals(a.success.board().tiles(), b.success.board().tiles());
    }

    @Test
    void refusesWhileAGameIsInProgress() {
        repo.game = TestGames.playingGame(1, "Ann", "Bob");
        RecordingPresenter presenter = new RecordingPresenter();
        new GenerateBoardInteractor(repo, presenter, new RandomLayoutStrategy())
                .execute(GenerateBoardInputData.random());
        assertNotNull(presenter.error);
        assertNull(presenter.success);
        assertNull(repo.board);
    }
}
