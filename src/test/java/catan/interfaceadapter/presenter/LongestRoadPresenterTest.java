package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.usecase.longestroad.LongestRoadOutputData;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/**
 * Tests for LongestRoadPresenter.
 */
class LongestRoadPresenterTest {

  @Test
  void presenterUpdatesViewModel() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);

    presenter.present("Alice", 6);

    assertEquals("Alice", viewModel.getHolderName());
    assertEquals(6, viewModel.getRoadLength());
  }

  @Test
  void presenterReplacesPreviousHolder() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);

    presenter.present("Alice", 5);
    presenter.present("Bob", 7);

    assertEquals("Bob", viewModel.getHolderName());
    assertEquals(7, viewModel.getRoadLength());
  }

  @Test
  void presenterClearsHolder() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);

    presenter.present("Alice", 6);
    presenter.present(null, 0);

    assertNull(viewModel.getHolderName());
    assertEquals(0, viewModel.getRoadLength());
  }

  @Test
  void presenterNotifiesViewModelListeners() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);
    AtomicInteger notificationCount = new AtomicInteger();

    viewModel.addPropertyChangeListener(
            event -> notificationCount.incrementAndGet());

    presenter.present("Alice", 6);

    assertEquals(1, notificationCount.get());
  }

  @Test
  void presenterRejectsNullViewModel() {
    assertThrows(
            NullPointerException.class,
            () -> new LongestRoadPresenter(null));
  }

  @Test
  void presenterAcceptsOutputData() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);

    LongestRoadOutputData outputData =
            new LongestRoadOutputData("Alice", 6);

    presenter.present(outputData);

    assertEquals("Alice", viewModel.getHolderName());
    assertEquals(6, viewModel.getRoadLength());
  }
}
