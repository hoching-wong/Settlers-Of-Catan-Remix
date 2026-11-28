package catan.interfaceadapter.presenter;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/** A {@link System.Logger} fake that records what was logged. */
public class RecordingLogger implements System.Logger {

  public record Entry(Level level, String message) {}

  public final List<Entry> entries = new ArrayList<>();

  @Override
  public String getName() {
    return "recording";
  }

  @Override
  public boolean isLoggable(Level level) {
    return true;
  }

  @Override
  public void log(Level level, ResourceBundle bundle, String msg, Throwable thrown) {
    entries.add(new Entry(level, msg));
  }

  @Override
  public void log(Level level, ResourceBundle bundle, String format, Object... params) {
    entries.add(
        new Entry(
            level,
            params == null || params.length == 0
                ? format
                : java.text.MessageFormat.format(format, params)));
  }
}
