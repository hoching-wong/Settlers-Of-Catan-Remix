package catan.usecase.recordwin;

import static org.junit.jupiter.api.Assertions.*;

import catan.usecase.port.FakeWinRecordRepository;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordStorageException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class RecordWinInteractorTest {

    private static class RecordingPresenter implements RecordWinOutputBoundary {
        final List<RecordWinOutputData> recorded = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentWinRecorded(RecordWinOutputData outputData) {
            recorded.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }
    }

    private final FakeWinRecordRepository repository = new FakeWinRecordRepository();
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final RecordWinInteractor interactor = new RecordWinInteractor(repository, presenter);

    private void record(String name) {
        interactor.recordWin(new RecordWinInputData(name));
    }

    private void assertSingleError() {
        assertEquals(1, presenter.errors.size());
        assertFalse(presenter.errors.get(0).isBlank());
        assertTrue(presenter.recorded.isEmpty());
    }

    @Test
    void firstTimeWinnerIsAppendedWithOneWin() {
        repository.stored = List.of(new WinRecord("Bob", 1));

        record("Alice");

        assertEquals(List.of(new WinRecord("Bob", 1), new WinRecord("Alice", 1)), repository.stored);
        assertEquals(List.of(new RecordWinOutputData("Alice", 1, false)), presenter.recorded);
        assertTrue(presenter.errors.isEmpty());
    }

    @Test
    void existingWinnerGainsOneWinInPlace() {
        repository.stored =
                List.of(new WinRecord("Bob", 5), new WinRecord("Alice", 2), new WinRecord("Carol", 1));

        record("Alice");

        assertEquals(
                List.of(new WinRecord("Bob", 5), new WinRecord("Alice", 3), new WinRecord("Carol", 1)),
                repository.stored);
        assertEquals(List.of(new RecordWinOutputData("Alice", 3, false)), presenter.recorded);
    }

    @Test
    void caseAndWhitespaceDifferencesCountAsTheSameWinner() {
        repository.stored = List.of(new WinRecord("Alice", 2));

        record(" ALICE ");

        assertEquals(List.of(new WinRecord("Alice", 3)), repository.stored);
        assertEquals("Alice", presenter.recorded.get(0).playerName());
    }

    @Test
    void loadsOnceThenSavesOnce() {
        record("Alice");

        assertEquals(List.of("load", "save"), repository.calls);
    }

    @Test
    void reportsAnErrorWithoutSavingWhenLoadFails() {
        repository.loadFailure = new WinRecordStorageException("cannot read");

        record("Alice");

        assertSingleError();
        assertEquals(List.of("load"), repository.calls);
    }

    @Test
    void setsCorruptedDataAsideAndRecordsFromScratch() {
        repository.stored = List.of(new WinRecord("Bob", 4));
        repository.corrupted = true;

        record("Alice");

        assertEquals(List.of("load", "discard", "save"), repository.calls);
        assertEquals(List.of(new WinRecord("Alice", 1)), repository.stored);
        assertEquals(List.of(new RecordWinOutputData("Alice", 1, true)), presenter.recorded);
        assertTrue(presenter.errors.isEmpty());
    }

    @Test
    void reportsAnErrorWithoutSavingWhenSettingCorruptedDataAsideFails() {
        repository.corrupted = true;
        repository.discardFailure = new WinRecordStorageException("cannot set aside");

        record("Alice");

        assertSingleError();
        assertEquals(List.of("load", "discard"), repository.calls);
    }

    @Test
    void reportsAnErrorWhenSaveFailsAndLeavesNoExceptionBehind() {
        repository.stored = List.of(new WinRecord("Alice", 2));
        repository.saveFailure = new WinRecordStorageException("cannot write");

        assertDoesNotThrow(() -> record("Alice"));

        assertSingleError();
        assertEquals(List.of(new WinRecord("Alice", 2)), repository.stored);
    }
}
