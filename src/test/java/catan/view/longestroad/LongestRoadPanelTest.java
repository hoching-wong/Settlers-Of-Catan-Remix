package catan.view.longestroad;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.interfaceadapters.longestroad.LongestRoadPresenter;
import catan.interfaceadapters.longestroad.LongestRoadViewModel;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Tests for LongestRoadPanel. */
class LongestRoadPanelTest {

  /**
   * Checks whether the panel contains a label with the expected text.
   *
   * @param panel the panel to inspect
   * @param text the expected label text
   * @return true if the label exists
   */
  private boolean containsLabel(LongestRoadPanel panel, String text) {
    for (Component component : panel.getComponents()) {
      if (component instanceof JLabel label && text.equals(label.getText())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Waits for pending Swing GUI updates to complete.
   *
   * @throws Exception if the event dispatch thread is interrupted
   */
  private void flushSwingEvents() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      // Wait for pending GUI updates.
    });
  }

  @Test
  void initialStateShowsNoHolder() {
    LongestRoadPanel panel = new LongestRoadPanel();

    assertTrue(containsLabel(panel, "Current Holder: None"));
    assertTrue(containsLabel(panel, "Longest Road Length: -"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: 0 VP"));
  }

  @Test
  void updateDisplaysHolderInformation() {
    LongestRoadPanel panel = new LongestRoadPanel();

    panel.updateLongestRoad("Alice", 6);

    assertTrue(containsLabel(panel, "Current Holder: Alice"));
    assertTrue(containsLabel(panel, "Longest Road Length: 6"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: +2 VP"));
  }

  @Test
  void updateWithNullHolderShowsNoHolder() {
    LongestRoadPanel panel = new LongestRoadPanel();

    panel.updateLongestRoad("Alice", 6);
    panel.updateLongestRoad(null, 0);

    assertTrue(containsLabel(panel, "Current Holder: None"));
    assertTrue(containsLabel(panel, "Longest Road Length: -"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: 0 VP"));
  }

  @Test
  void updateReplacesPreviousHolder() {
    LongestRoadPanel panel = new LongestRoadPanel();

    panel.updateLongestRoad("Alice", 5);
    panel.updateLongestRoad("Bob", 7);

    assertTrue(containsLabel(panel, "Current Holder: Bob"));
    assertTrue(containsLabel(panel, "Longest Road Length: 7"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: +2 VP"));
  }

  @Test
  void panelContainsFourLabels() {
    LongestRoadPanel panel = new LongestRoadPanel();

    assertEquals(4, panel.getComponentCount());
  }

  @Test
  void viewModelUpdatesPanelAutomatically() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    viewModel.updateState("Alice", 6);
    flushSwingEvents();

    assertTrue(containsLabel(panel, "Current Holder: Alice"));
    assertTrue(containsLabel(panel, "Longest Road Length: 6"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: +2 VP"));
  }

  @Test
  void presenterUpdatesPanelThroughViewModel() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    LongestRoadPresenter presenter = new LongestRoadPresenter(viewModel);

    LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    presenter.present("Bob", 7);
    flushSwingEvents();

    assertTrue(containsLabel(panel, "Current Holder: Bob"));
    assertTrue(containsLabel(panel, "Longest Road Length: 7"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: +2 VP"));
  }

  @Test
  void clearingViewModelClearsPanel() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    final LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    viewModel.updateState("Alice", 6);
    flushSwingEvents();

    viewModel.updateState(null, 0);
    flushSwingEvents();

    assertTrue(containsLabel(panel, "Current Holder: None"));
    assertTrue(containsLabel(panel, "Longest Road Length: -"));
    assertTrue(containsLabel(panel, "Victory Point Bonus: 0 VP"));
  }
}
