package catan.framework.persistence;

import static org.junit.jupiter.api.Assertions.*;

import catan.usecase.port.CorruptedWinRecordsException;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordStorageException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JsonFileWinRecordRepositoryTest {

    @TempDir Path dir;

    private Path file() {
        return dir.resolve("game_stats.json");
    }

    private Path tmp() {
        return dir.resolve("game_stats.json.tmp");
    }

    private Path bak() {
        return dir.resolve("game_stats.json.bak");
    }

    private JsonFileWinRecordRepository repository() {
        return new JsonFileWinRecordRepository(file());
    }

    private void write(String content) throws IOException {
        Files.writeString(file(), content, StandardCharsets.UTF_8);
    }

    private static WinRecord r(String name, int wins) {
        return new WinRecord(name, wins);
    }

    // ---- load ----

    @Test
    void loadReturnsNothingWhenTheFileDoesNotExist() throws Exception {
        assertEquals(List.of(), repository().load());
        assertFalse(Files.exists(file()));
    }

    @Test
    void loadReadsHandWrittenRecordsInFileOrder() throws Exception {
        write("{\"records\":[{\"name\":\"Bob\",\"wins\":1},{\"name\":\"Alice\",\"wins\":3}]}");

        assertEquals(List.of(r("Bob", 1), r("Alice", 3)), repository().load());
    }

    @Test
    void loadIgnoresUnknownFields() throws Exception {
        write("{\"version\":2,\"records\":[{\"name\":\"Alice\",\"wins\":3,\"last\":\"2026\"}]}");

        assertEquals(List.of(r("Alice", 3)), repository().load());
    }

    @Test
    void loadReportsTruncatedJsonAsCorrupted() throws Exception {
        write("{\"records\":[{\"name\":\"Alice\",\"wi");

        assertThrows(CorruptedWinRecordsException.class, () -> repository().load());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  \n"})
    void loadReportsEmptyOrBlankFileAsCorrupted(String content) throws Exception {
        write(content);

        assertThrows(CorruptedWinRecordsException.class, () -> repository().load());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "[]",
        "{}",
        "{\"records\":{}}",
        "{\"records\":[\"Alice\"]}",
    })
    void loadReportsAWrongShapeAsCorrupted(String content) throws Exception {
        write(content);

        assertThrows(CorruptedWinRecordsException.class, () -> repository().load());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"wins\":1}",
        "{\"name\":5,\"wins\":1}",
        "{\"name\":\"   \",\"wins\":1}",
        "{\"name\":\"Alice\"}",
        "{\"name\":\"Alice\",\"wins\":\"3\"}",
        "{\"name\":\"Alice\",\"wins\":3.5}",
        "{\"name\":\"Alice\",\"wins\":-1}",
    })
    void loadReportsAnInvalidEntryAsCorrupted(String entry) throws Exception {
        write("{\"records\":[" + entry + "]}");

        assertThrows(CorruptedWinRecordsException.class, () -> repository().load());
    }

    @Test
    void loadReportsNamesThatAreTheSamePlayerAsCorrupted() throws Exception {
        write("{\"records\":[{\"name\":\"Alice\",\"wins\":1},{\"name\":\" alice \",\"wins\":2}]}");

        assertThrows(CorruptedWinRecordsException.class, () -> repository().load());
    }

    @Test
    void loadReportsAnUnreadablePathAsAPlainStorageFailure() throws Exception {
        Files.createDirectory(file());

        WinRecordStorageException e = assertThrows(WinRecordStorageException.class, () -> repository().load());
        assertFalse(e instanceof CorruptedWinRecordsException);
    }

    // ---- save ----

    @Test
    void savedRecordsCanBeReadByAnotherInstance() throws Exception {
        List<WinRecord> records = List.of(r("Alice", 3), r("太郎", 1));
        repository().save(records);

        assertEquals(records, new JsonFileWinRecordRepository(file()).load());
    }

    @Test
    void saveWritesTheDocumentedFormat() throws Exception {
        repository().save(List.of(r("Alice", 3)));

        JSONObject root = new JSONObject(Files.readString(file(), StandardCharsets.UTF_8));
        JSONArray records = root.getJSONArray("records");
        assertEquals(1, records.length());
        assertEquals("Alice", records.getJSONObject(0).getString("name"));
        assertEquals(3, records.getJSONObject(0).getInt("wins"));
    }

    @Test
    void saveCreatesMissingParentDirectories() throws Exception {
        Path nested = dir.resolve("a").resolve("data").resolve("game_stats.json");
        new JsonFileWinRecordRepository(nested).save(List.of(r("Alice", 1)));

        assertEquals(List.of(r("Alice", 1)), new JsonFileWinRecordRepository(nested).load());
    }

    @Test
    void saveReplacesThePreviousContent() throws Exception {
        repository().save(List.of(r("Alice", 3), r("Bob", 2)));
        repository().save(List.of(r("Carol", 1)));

        assertEquals(List.of(r("Carol", 1)), repository().load());
    }

    @Test
    void saveWritesAReadableFileForAnEmptyList() throws Exception {
        repository().save(List.of());

        assertEquals(List.of(), repository().load());
        assertTrue(Files.exists(file()));
    }

    @Test
    void saveLeavesNoTemporaryFileBehind() throws Exception {
        repository().save(List.of(r("Alice", 1)));

        assertFalse(Files.exists(tmp()));
    }

    @Test
    void aFailedSaveLeavesTheStoredRecordsUntouched() throws Exception {
        repository().save(List.of(r("Alice", 3)));
        Files.createDirectory(tmp());

        assertThrows(WinRecordStorageException.class, () -> repository().save(List.of(r("Bob", 9))));

        assertEquals(List.of(r("Alice", 3)), repository().load());
    }

    // ---- discardCorrupted ----

    @Test
    void discardMovesTheFileToBakAndEmptiesTheStorage() throws Exception {
        write("not json");

        repository().discardCorrupted();

        assertFalse(Files.exists(file()));
        assertEquals("not json", Files.readString(bak(), StandardCharsets.UTF_8));
        assertEquals(List.of(), repository().load());
    }

    @Test
    void discardOverwritesAnExistingBak() throws Exception {
        Files.writeString(bak(), "older", StandardCharsets.UTF_8);
        write("newer");

        repository().discardCorrupted();

        assertEquals("newer", Files.readString(bak(), StandardCharsets.UTF_8));
    }

    @Test
    void discardDoesNothingWhenThereIsNoFile() throws Exception {
        repository().discardCorrupted();

        assertFalse(Files.exists(file()));
        assertFalse(Files.exists(bak()));
    }

    @Test
    void aFailedDiscardKeepsTheFileAndReportsAnError() throws Exception {
        write("not json");
        Files.createDirectory(bak());
        Files.writeString(bak().resolve("keep"), "x", StandardCharsets.UTF_8);

        assertThrows(WinRecordStorageException.class, () -> repository().discardCorrupted());

        assertEquals("not json", Files.readString(file(), StandardCharsets.UTF_8));
    }
}
