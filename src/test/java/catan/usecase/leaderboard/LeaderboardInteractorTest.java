package catan.usecase.leaderboard;

import static org.junit.jupiter.api.Assertions.*;

import catan.usecase.leaderboard.LeaderboardOutputData.Row;
import catan.usecase.port.FakeWinRecordRepository;
import catan.usecase.port.WinRecord;
import catan.usecase.port.WinRecordStorageException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardInteractorTest {

  private static class RecordingPresenter implements LeaderboardOutputBoundary {
    final List<LeaderboardOutputData> shown = new ArrayList<>();
    final List<String> userMessages = new ArrayList<>();
    final List<String> details = new ArrayList<>();

    @Override
    public void presentLeaderboard(LeaderboardOutputData outputData) {
      shown.add(outputData);
    }

    @Override
    public void presentError(String userMessage, String detail) {
      userMessages.add(userMessage);
      details.add(detail);
    }
  }

  private final FakeWinRecordRepository repository = new FakeWinRecordRepository();
  private final RecordingPresenter presenter = new RecordingPresenter();
  private final LeaderboardInteractor interactor = new LeaderboardInteractor(repository, presenter);

  private List<Row> show(WinRecord... records) {
    repository.stored = List.of(records);
    interactor.showLeaderboard(new LeaderboardInputData());
    assertEquals(1, presenter.shown.size());
    assertTrue(presenter.userMessages.isEmpty());
    return presenter.shown.get(0).rows();
  }

  private static WinRecord r(String name, int wins) {
    return new WinRecord(name, wins);
  }

  @Test
  void ordersByWinsDescending() {
    List<Row> rows = show(r("A", 1), r("B", 3), r("C", 2));

    assertEquals(List.of("B", "C", "A"), rows.stream().map(Row::name).toList());
  }

  @Test
  void ordersTiesByNameIgnoringCase() {
    List<Row> rows = show(r("bob", 2), r("Alice", 2));

    assertEquals(List.of("Alice", "bob"), rows.stream().map(Row::name).toList());
  }

  @Test
  void ranksInCompetitionStyle() {
    List<Row> rows = show(r("A", 5), r("B", 3), r("C", 3), r("D", 1));

    assertEquals(List.of(1, 2, 2, 4), rows.stream().map(Row::rank).toList());
  }

  @Test
  void everyoneTiedMeansEveryoneIsFirst() {
    List<Row> rows = show(r("A", 3), r("B", 3), r("C", 3));

    assertEquals(List.of(1, 1, 1), rows.stream().map(Row::rank).toList());
  }

  @Test
  void showsOnlyTheTopFiveRowsWithOverallRanks() {
    List<Row> rows =
        show(r("A", 7), r("B", 6), r("C", 5), r("D", 4), r("E", 3), r("F", 2), r("G", 1));

    assertEquals(5, rows.size());
    assertEquals(List.of(1, 2, 3, 4, 5), rows.stream().map(Row::rank).toList());
    assertEquals("E", rows.get(4).name());
  }

  @Test
  void aTieAcrossTheFifthRowIsCutOff() {
    List<Row> rows = show(r("A", 5), r("B", 4), r("C", 3), r("D", 2), r("E", 1), r("F", 1));

    assertEquals(List.of("A", "B", "C", "D", "E"), rows.stream().map(Row::name).toList());
    assertEquals(5, rows.get(4).rank());
  }

  @Test
  void showsEveryRecordWhenThereAreFewerThanFive() {
    assertEquals(3, show(r("A", 1), r("B", 2), r("C", 3)).size());
  }

  @Test
  void keepsTheStoredSpellingOfNames() {
    List<Row> rows = show(r("Alice", 2));

    assertEquals(new Row(1, "Alice", 2), rows.get(0));
  }

  @Test
  void noRecordsIsASuccessWithNoRows() {
    List<Row> rows = show();

    assertTrue(rows.isEmpty());
  }

  @Test
  void reportsOneErrorWhenLoadFails() {
    repository.loadFailure = new WinRecordStorageException("cannot read the file");

    interactor.showLeaderboard(new LeaderboardInputData());

    assertTrue(presenter.shown.isEmpty());
    assertEquals(1, presenter.userMessages.size());
    assertFalse(presenter.userMessages.get(0).isBlank());
    assertTrue(presenter.details.get(0).contains("cannot read the file"));
  }

  @Test
  void doesNotTouchTheFileWhenTheDataIsCorrupted() {
    repository.corrupted = true;

    interactor.showLeaderboard(new LeaderboardInputData());

    assertEquals(List.of("load"), repository.calls);
    assertTrue(presenter.shown.isEmpty());
    assertEquals(1, presenter.userMessages.size());
  }
}
