package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.LeaderboardRow;
import catan.interfaceadapter.viewmodel.LeaderboardViewModel;
import catan.usecase.leaderboard.LeaderboardOutputData;
import catan.usecase.leaderboard.LeaderboardOutputData.Row;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardPresenterTest {

  private final LeaderboardViewModel viewModel = new LeaderboardViewModel();
  private final RecordingLogger logger = new RecordingLogger();
  private final LeaderboardPresenter presenter = new LeaderboardPresenter(viewModel, logger);

  @Test
  void putsTheRowsIntoTheViewModelInOrder() {
    presenter.presentLeaderboard(
        new LeaderboardOutputData(
            List.of(new Row(1, "Alice", 3), new Row(2, "Bob", 1), new Row(2, "Carol", 1))));

    assertEquals(
        List.of(
            new LeaderboardRow(1, "Alice", 3),
            new LeaderboardRow(2, "Bob", 1),
            new LeaderboardRow(2, "Carol", 1)),
        viewModel.getRows());
  }

  @Test
  void anEmptyResultGivesNoRowsAndNoError() {
    presenter.presentError("old error", "old detail");

    presenter.presentLeaderboard(new LeaderboardOutputData(List.of()));

    assertEquals(List.of(), viewModel.getRows());
    assertEquals("", viewModel.getErrorMessage());
  }

  @Test
  void anErrorShowsTheUserMessageAndLogsTheDetail() {
    presenter.presentError(
        "The win records could not be loaded.", "java.io.IOException: secret detail");

    assertEquals("The win records could not be loaded.", viewModel.getErrorMessage());
    assertFalse(viewModel.getErrorMessage().contains("secret detail"));
    assertEquals(List.of(), viewModel.getRows());
    assertEquals(1, logger.entries.size());
    assertEquals(System.Logger.Level.WARNING, logger.entries.get(0).level());
    assertTrue(logger.entries.get(0).message().contains("secret detail"));
  }

  @Test
  void logsNothingOnSuccess() {
    presenter.presentLeaderboard(new LeaderboardOutputData(List.of(new Row(1, "Alice", 3))));

    assertTrue(logger.entries.isEmpty());
  }
}
