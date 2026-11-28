package catan.interfaceadapter.viewmodel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardViewModelTest {

  private final LeaderboardViewModel viewModel = new LeaderboardViewModel();

  @Test
  void startsWithNoRowsAndNoError() {
    assertEquals(List.of(), viewModel.getRows());
    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void showRowsKeepsTheRowsInOrderAndClearsThePreviousError() {
    viewModel.showError("boom");
    List<LeaderboardRow> rows =
        List.of(new LeaderboardRow(1, "Alice", 3), new LeaderboardRow(2, "Bob", 1));

    viewModel.showRows(rows);

    assertEquals(rows, viewModel.getRows());
    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void showErrorKeepsTheMessageAndClearsThePreviousRows() {
    viewModel.showRows(List.of(new LeaderboardRow(1, "Alice", 3)));

    viewModel.showError("The win records could not be loaded.");

    assertEquals("The win records could not be loaded.", viewModel.getErrorMessage());
    assertEquals(List.of(), viewModel.getRows());
  }

  @Test
  void notifiesListenersOnEveryChange() {
    List<String> names = new ArrayList<>();
    viewModel.addPropertyChangeListener(e -> names.add(e.getPropertyName()));

    viewModel.showRows(List.of());
    viewModel.showError("boom");

    assertEquals(
        List.of(LeaderboardViewModel.LEADERBOARD, LeaderboardViewModel.LEADERBOARD), names);
    assertEquals("leaderboard", LeaderboardViewModel.LEADERBOARD);
  }

  @Test
  void keepsACopyOfTheRows() {
    List<LeaderboardRow> rows = new ArrayList<>(List.of(new LeaderboardRow(1, "Alice", 3)));
    viewModel.showRows(rows);

    rows.clear();

    assertEquals(1, viewModel.getRows().size());
  }
}
