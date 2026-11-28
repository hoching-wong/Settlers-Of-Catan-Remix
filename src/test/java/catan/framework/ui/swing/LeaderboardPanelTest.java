package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static catan.framework.ui.swing.SwingTestSupport.flush;
import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.LeaderboardRow;
import catan.interfaceadapter.viewmodel.LeaderboardViewModel;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JTable;
import org.junit.jupiter.api.Test;

/** Drives the real Swing components without showing a window. */
class LeaderboardPanelTest {

  private final LeaderboardViewModel viewModel = new LeaderboardViewModel();
  private final LeaderboardPanel panel = new LeaderboardPanel(viewModel);

  private JTable table() {
    return find(panel, "leaderboardTable", JTable.class);
  }

  private JLabel message() {
    return find(panel, "leaderboardMessage", JLabel.class);
  }

  private void assertShowsNoRecords() {
    assertFalse(table().isVisible());
    assertTrue(message().isVisible());
    assertEquals("No records yet.", message().getText());
  }

  @Test
  void startsByShowingThatThereAreNoRecords() {
    assertShowsNoRecords();
  }

  @Test
  void showsRowsWithRankNameAndWins() {
    viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 3), new LeaderboardRow(2, "Bob", 1)));
    flush();

    assertTrue(table().isVisible());
    assertFalse(message().isVisible());
    assertEquals(
        List.of("Rank", "Name", "Wins"),
        List.of(table().getColumnName(0), table().getColumnName(1), table().getColumnName(2)));
    assertEquals(2, table().getRowCount());
    assertEquals("1", String.valueOf(table().getValueAt(0, 0)));
    assertEquals("Alice", table().getValueAt(0, 1));
    assertEquals("3", String.valueOf(table().getValueAt(0, 2)));
    assertEquals("Bob", table().getValueAt(1, 1));
  }

  @Test
  void showsTiedRanksAsGiven() {
    viewModel.showRows(
        List.of(
            new LeaderboardRow(1, "A", 5),
            new LeaderboardRow(2, "B", 3),
            new LeaderboardRow(2, "C", 3)));
    flush();

    assertEquals(
        List.of("1", "2", "2"),
        List.of(
            String.valueOf(table().getValueAt(0, 0)),
            String.valueOf(table().getValueAt(1, 0)),
            String.valueOf(table().getValueAt(2, 0))));
  }

  @Test
  void showsAnErrorInPlaceOfTheTable() {
    viewModel.showError("The win records could not be loaded.");
    flush();

    assertFalse(table().isVisible());
    assertTrue(message().isVisible());
    assertEquals("The win records could not be loaded.", message().getText());
  }

  @Test
  void recoversFromAnErrorWhenRowsArrive() {
    viewModel.showError("boom");
    viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 1)));
    flush();

    assertTrue(table().isVisible());
    assertFalse(message().isVisible());
    assertEquals(1, table().getRowCount());
  }

  @Test
  void goesBackToNoRecordsWhenAnEmptyResultArrives() {
    viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 1)));
    viewModel.showRows(List.of());
    flush();

    assertShowsNoRecords();
    assertEquals(0, table().getRowCount());
  }

  @Test
  void followsViewModelChangesMadeOffTheSwingThread() throws Exception {
    Thread worker =
        new Thread(() -> viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 2))));
    worker.start();
    worker.join();
    flush();

    assertEquals(1, table().getRowCount());
    assertEquals("Alice", table().getValueAt(0, 1));
  }

  @Test
  void cellsCannotBeEdited() {
    viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 2)));
    flush();

    assertFalse(table().isCellEditable(0, 1));
  }
}
