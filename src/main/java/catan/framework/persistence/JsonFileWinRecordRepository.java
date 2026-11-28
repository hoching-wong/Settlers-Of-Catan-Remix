package catan.framework.persistence;

import catan.usecase.port.CorruptedWinRecordsException;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordRepository;
import catan.usecase.port.WinRecordStorageException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Keeps the win records in one JSON file: {@code {"records":[{"name":"Alice","wins":3}]}}.
 *
 * <p>Writing goes to {@code <file>.tmp} first and is then moved over the real file, so a failed
 * write never damages the stored records. Unreadable data is set aside as {@code <file>.bak}.
 */
public class JsonFileWinRecordRepository implements WinRecordRepository {

  private final Path file;

  /**
   * Creates a repository that keeps its records in one file.
   *
   * @param file the JSON file, for example {@code data/game_stats.json}
   */
  public JsonFileWinRecordRepository(Path file) {
    this.file = Objects.requireNonNull(file, "file");
  }

  @Override
  public List<WinRecord> load() throws WinRecordStorageException {
    if (!Files.exists(file)) {
      return new ArrayList<>();
    }
    String text;
    try {
      text = Files.readString(file, StandardCharsets.UTF_8);
    } catch (CharacterCodingException e) {
      throw new CorruptedWinRecordsException("The file is not valid UTF-8: " + file, e);
    } catch (IOException e) {
      throw new WinRecordStorageException("Cannot read " + file, e);
    }
    try {
      return parse(text);
    } catch (JSONException | IllegalArgumentException e) {
      throw new CorruptedWinRecordsException(
          "Cannot understand " + file + ": " + e.getMessage(), e);
    }
  }

  private static List<WinRecord> parse(String text) {
    JSONArray array = new JSONObject(text).optJSONArray("records");
    if (array == null) {
      throw new JSONException("\"records\" is missing or is not an array");
    }
    List<WinRecord> records = new ArrayList<>();
    Set<String> keys = new HashSet<>();
    for (int i = 0; i < array.length(); i++) {
      JSONObject entry = array.optJSONObject(i);
      if (entry == null) {
        throw new JSONException("Entry " + i + " is not an object");
      }
      if (!(entry.opt("name") instanceof String name)) {
        throw new JSONException("Entry " + i + " has no text \"name\"");
      }
      if (!(entry.opt("wins") instanceof Integer wins)) {
        throw new JSONException("Entry " + i + " has no whole-number \"wins\"");
      }
      WinRecord record = new WinRecord(name, wins); // throws IllegalArgumentException if invalid
      if (!keys.add(record.key())) {
        throw new JSONException("Entry " + i + " repeats the player " + record.name());
      }
      records.add(record);
    }
    return records;
  }

  @Override
  public void save(List<WinRecord> records) throws WinRecordStorageException {
    JSONArray array = new JSONArray();
    for (WinRecord record : records) {
      array.put(new JSONObject().put("name", record.name()).put("wins", record.wins()));
    }
    String text = new JSONObject().put("records", array).toString(2);
    Path tmp = file.resolveSibling(file.getFileName() + ".tmp");
    try {
      Path parent = file.toAbsolutePath().getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      Files.writeString(tmp, text, StandardCharsets.UTF_8);
      try {
        Files.move(tmp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } catch (IOException e) {
        Files.deleteIfExists(tmp);
        throw e;
      }
    } catch (IOException e) {
      throw new WinRecordStorageException("Cannot write " + file, e);
    }
  }

  @Override
  public void discardCorrupted() throws WinRecordStorageException {
    if (!Files.exists(file)) {
      return;
    }
    Path bak = file.resolveSibling(file.getFileName() + ".bak");
    try {
      Files.move(file, bak, StandardCopyOption.REPLACE_EXISTING);
    } catch (IOException e) {
      throw new WinRecordStorageException("Cannot set " + file + " aside", e);
    }
  }
}
