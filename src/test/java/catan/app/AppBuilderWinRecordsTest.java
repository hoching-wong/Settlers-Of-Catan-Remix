package catan.app;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.Settlement;
import catan.entity.Vertex;
import catan.framework.persistence.InMemoryGameRepository;
import catan.framework.persistence.JsonFileWinRecordRepository;
import catan.framework.ui.swing.BoardPanel;
import catan.framework.ui.swing.LeaderboardPanel;
import catan.framework.ui.swing.ScreenSwitcher;
import catan.interfaceadapter.viewmodel.BoardPoint;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.usecase.TestGames;
import catan.usecase.port.FakeWinRecordRepository;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordRepository;
import catan.usecase.port.WinRecordStorageException;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * UC9 through the whole application: a win is recorded, kept in a file and shown on the
 * leaderboard.
 */
class AppBuilderWinRecordsTest {

  @TempDir Path dir;

  private final InMemoryGameRepository games = new InMemoryGameRepository();
  private final List<LeaderboardPanel> openedPanels = new ArrayList<>();
  private AppBuilder builder;
  private ScreenSwitcher app;

  private Path statsFile() {
    return dir.resolve("data").resolve("game_stats.json");
  }

  /** Builds the application the way Main does, except that the leaderboard dialog is not opened. */
  private void build(WinRecordRepository winRecords) {
    builder = new AppBuilder(new Random(5), city -> 0, games, winRecords, Runnable::run);
    builder.setLeaderboardOpener((window, panel) -> openedPanels.add(panel));
    app = builder.buildScreens();
  }

  private void click(String name) {
    find(app, name, JButton.class).doClick();
    flush();
  }

  private JTable pressLeaderboardAndGetTable() {
    click("leaderboard");
    return find(openedPanels.get(openedPanels.size() - 1), "leaderboardTable", JTable.class);
  }

  private JLabel pressLeaderboardAndGetMessage() {
    click("leaderboard");
    return find(openedPanels.get(openedPanels.size() - 1), "leaderboardMessage", JLabel.class);
  }

  private static String cell(JTable table, int row, int column) {
    return String.valueOf(table.getValueAt(row, column));
  }

  private static <T extends Component> T find(Container root, String name, Class<T> type) {
    for (Component c : root.getComponents()) {
      if (name.equals(c.getName()) && type.isInstance(c)) {
        return type.cast(c);
      }
      if (c instanceof Container inner) {
        T found = find(inner, name, type);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static void flush() {
    try {
      SwingUtilities.invokeAndWait(() -> {});
    } catch (InterruptedException | InvocationTargetException e) {
      throw new AssertionError(e);
    }
  }

  // ---- driving a game ----

  private void startAGame(String... names) {
    find(app, "playerCount", JComboBox.class).setSelectedItem(names.length);
    for (int i = 0; i < names.length; i++) {
      find(app, "name" + (i + 1), JTextField.class).setText(names[i]);
    }
    find(app, "city", JComboBox.class).setSelectedItem("Toronto");
    click("start");
  }

  private void finishSetup(Game game) {
    while (game.getPhase() == GamePhase.SETUP) {
      Player p = game.getCurrentPlayer();
      Vertex spot =
          game.getBoard().getVertices().stream()
              .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
              .findFirst()
              .orElseThrow();
      builder.getPlaceInitialController().place(spot.getId());
      builder
          .getPlaceInitialController()
          .place(
              game.getLastPlacedSettlement().getAdjacentEdges().stream()
                  .filter(e -> !e.isOccupied())
                  .findFirst()
                  .orElseThrow()
                  .getId());
    }
    flush();
  }

  private static AbstractButton buttonWithText(Container root, String text) {
    for (Component c : root.getComponents()) {
      if (c instanceof AbstractButton b && text.equals(b.getText())) {
        return b;
      }
      if (c instanceof Container inner) {
        AbstractButton found = buttonWithText(inner, text);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private static BoardPanel findBoardPanel(Container root) {
    for (Component c : root.getComponents()) {
      if (c instanceof BoardPanel panel) {
        return panel;
      }
      if (c instanceof Container inner) {
        BoardPanel found = findBoardPanel(inner);
        if (found != null) {
          return found;
        }
      }
    }
    return null;
  }

  private void clickVertex(int id) {
    BoardState state = builder.getBoardViewModel().getState();
    BoardPoint p = state.getVertices().get(id).position();
    BoardPanel panel = findBoardPanel(app);
    panel.setSize(720, 640);
    double scale = Math.min(720 / (2 * (Math.sqrt(3) * 2.5 + 0.55)), 640 / (2 * (4.0 + 0.55)));
    int x = (int) Math.round(360 + p.x() * scale);
    int y = (int) Math.round(320 + p.y() * scale);
    panel.dispatchEvent(
        new MouseEvent(
            panel,
            MouseEvent.MOUSE_CLICKED,
            System.currentTimeMillis(),
            0,
            x,
            y,
            1,
            false,
            MouseEvent.BUTTON1));
    flush();
  }

  /** Ann (first player) wins by upgrading a settlement to a city from the build menu. */
  private Game playUntilAnnWinsWithAnUpgrade() {
    startAGame("Ann", "Bob");
    Game game = games.get();
    finishSetup(game);
    game.markRolled();
    Player ann = game.getCurrentPlayer();
    Board board = game.getBoard();
    for (Vertex v : board.getVertices()) {
      if (ann.getVictoryPoints() >= 9) {
        break;
      }
      if (board.isValidSettlementLocation(v, ann, true)) {
        board.placeBuilding(v, new Settlement(ann, v));
      }
    }
    assertEquals(9, ann.getVictoryPoints());
    TestGames.give(ann, 5);

    buttonWithText(app, "City").doClick();
    flush();
    int target = builder.getBoardViewModel().getState().getHighlightedVertexIds().iterator().next();
    clickVertex(target);
    return game;
  }

  // ---- the tests ----

  @Test
  void aWinBuiltOnTheBoardIsSavedToTheFileAndShownOnTheLeaderboard() throws Exception {
    build(new JsonFileWinRecordRepository(statsFile()));

    Game game = playUntilAnnWinsWithAnUpgrade();

    assertEquals(GamePhase.GAME_OVER, game.getPhase());
    assertEquals(
        List.of(new WinRecord("Ann", 1)), new JsonFileWinRecordRepository(statsFile()).load());
    JTable table = pressLeaderboardAndGetTable();
    assertEquals(1, table.getRowCount());
    assertEquals(
        List.of("1", "Ann", "1"), List.of(cell(table, 0, 0), cell(table, 0, 1), cell(table, 0, 2)));
  }

  @Test
  void theRecordsSurviveARestartOfTheApplication() {
    build(new JsonFileWinRecordRepository(statsFile()));
    playUntilAnnWinsWithAnUpgrade();

    openedPanels.clear();
    build(new JsonFileWinRecordRepository(statsFile())); // a new application on the same file
    JTable table = pressLeaderboardAndGetTable();

    assertEquals(1, table.getRowCount());
    assertEquals("Ann", table.getValueAt(0, 1));
  }

  @Test
  void aHandWrittenFileIsShownWithSharedRanks() throws Exception {
    Files.createDirectories(statsFile().getParent());
    Files.writeString(
        statsFile(),
        "{\"records\":[{\"name\":\"Cat\",\"wins\":1},{\"name\":\"Bob\",\"wins\":3},{\"name\":\"Ann\",\"wins\":3}]}",
        StandardCharsets.UTF_8);
    build(new JsonFileWinRecordRepository(statsFile()));

    JTable table = pressLeaderboardAndGetTable();

    assertEquals(3, table.getRowCount());
    assertEquals(
        List.of("Ann", "Bob", "Cat"),
        List.of(cell(table, 0, 1), cell(table, 1, 1), cell(table, 2, 1)));
    assertEquals(
        List.of("1", "1", "3"), List.of(cell(table, 0, 0), cell(table, 1, 0), cell(table, 2, 0)));
  }

  @Test
  void withoutAFileTheLeaderboardSaysThereAreNoRecords() {
    build(new JsonFileWinRecordRepository(statsFile()));

    JLabel message = pressLeaderboardAndGetMessage();

    assertTrue(message.isVisible());
    assertEquals("No records yet.", message.getText());
    assertFalse(Files.exists(statsFile()));
  }

  @Test
  void aCorruptedFileShowsAnErrorAndIsLeftAlone() throws Exception {
    Files.createDirectories(statsFile().getParent());
    Files.writeString(statsFile(), "not json", StandardCharsets.UTF_8);
    build(new JsonFileWinRecordRepository(statsFile()));

    JLabel message = pressLeaderboardAndGetMessage();

    assertEquals("The win records could not be loaded.", message.getText());
    assertEquals("not json", Files.readString(statsFile(), StandardCharsets.UTF_8));
    assertFalse(Files.exists(statsFile().resolveSibling("game_stats.json.bak")));
  }

  @Test
  void aWinIsStillAWinWhenTheRecordCannotBeSaved() {
    FakeWinRecordRepository failing = new FakeWinRecordRepository();
    failing.saveFailure = new WinRecordStorageException("disk full");
    build(failing);

    Game game = playUntilAnnWinsWithAnUpgrade();

    assertEquals(GamePhase.GAME_OVER, game.getPhase());
    assertEquals("Ann", game.getWinner().getName());
    assertFalse(builder.getBoardViewModel().getState().isError());
    assertTrue(failing.stored.isEmpty());
  }

  @Test
  void theStartMenuShowsTheLeaderboardButton() {
    build(new FakeWinRecordRepository());

    JButton button = find(app, "leaderboard", JButton.class);

    assertTrue(button.isVisible());
    assertTrue(button.isEnabled());
  }
}
