package catan.interfaceadapters.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

/** Tests for LongestRoadViewModel. */
class LongestRoadViewModelTest {

  @Test
  void initialStateHasNoHolder() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    assertNull(viewModel.getHolderName());
    assertEquals(0, viewModel.getRoadLength());
  }

  @Test
  void updateStateStoresHolderInformation() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    viewModel.updateState("Alice", 6);

    assertEquals("Alice", viewModel.getHolderName());
    assertEquals(6, viewModel.getRoadLength());
  }

  @Test
  void updateStateReplacesPreviousHolder() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    viewModel.updateState("Alice", 5);
    viewModel.updateState("Bob", 7);

    assertEquals("Bob", viewModel.getHolderName());
    assertEquals(7, viewModel.getRoadLength());
  }

  @Test
  void nullHolderResetsRoadLength() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    viewModel.updateState("Alice", 6);
    viewModel.updateState(null, 0);

    assertNull(viewModel.getHolderName());
    assertEquals(0, viewModel.getRoadLength());
  }

  @Test
  void updateStateNotifiesListeners() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    AtomicInteger notificationCount = new AtomicInteger();

    viewModel.addPropertyChangeListener(event -> {
      assertEquals("longestRoad", event.getPropertyName());
      notificationCount.incrementAndGet();
    });

    viewModel.updateState("Alice", 6);

    assertEquals(1, notificationCount.get());
  }

  @Test
  void multipleUpdatesNotifyListeners() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    AtomicInteger notificationCount = new AtomicInteger();

    viewModel.addPropertyChangeListener(event -> notificationCount.incrementAndGet());

    viewModel.updateState("Alice", 5);
    viewModel.updateState("Bob", 7);
    viewModel.updateState(null, 0);

    assertEquals(3, notificationCount.get());
  }
}
