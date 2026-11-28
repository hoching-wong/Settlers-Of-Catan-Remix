package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.usecase.recordwin.RecordWinOutputData;
import org.junit.jupiter.api.Test;

class RecordWinPresenterTest {

    private final RecordingLogger logger = new RecordingLogger();
    private final RecordWinPresenter presenter = new RecordWinPresenter(logger);

    @Test
    void logsARecordedWinAsInfo() {
        presenter.presentWinRecorded(new RecordWinOutputData("Alice", 3, false));

        assertEquals(1, logger.entries.size());
        RecordingLogger.Entry entry = logger.entries.get(0);
        assertEquals(System.Logger.Level.INFO, entry.level());
        assertTrue(entry.message().contains("Alice"));
        assertTrue(entry.message().contains("3"));
    }

    @Test
    void logsAWarningToo_whenCorruptedRecordsWereSetAside() {
        presenter.presentWinRecorded(new RecordWinOutputData("Alice", 1, true));

        assertEquals(2, logger.entries.size());
        assertTrue(logger.entries.stream().anyMatch(e -> e.level() == System.Logger.Level.WARNING));
        assertTrue(logger.entries.stream().anyMatch(e -> e.level() == System.Logger.Level.INFO));
    }

    @Test
    void logsAFailureAsError() {
        presenter.presentError("Could not record the win: disk full");

        assertEquals(1, logger.entries.size());
        assertEquals(System.Logger.Level.ERROR, logger.entries.get(0).level());
        assertTrue(logger.entries.get(0).message().contains("disk full"));
    }
}
