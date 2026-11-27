package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import catan.interfaceadapter.presenter.LongestRoadPresenter;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import catan.usecase.longestroad.LongestRoadOutputData;
import java.awt.Component;
import java.awt.Container;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Tests for LongestRoadPanel. */
class LongestRoadPanelTest {

  /**
   * Finds a named component recursively within the panel.
   *
   * @param component the component to inspect
   * @param name the expected component name
   * @return the matching component, or null if not found
   */
  private Component findNamedComponent(Component component, String name) {
    if (name.equals(component.getName())) {
      return component;
    }

    if (component instanceof Container container) {
      for (Component child : container.getComponents()) {
        Component found = findNamedComponent(child, name);

        if (found != null) {
          return found;
        }
      }
    }

    return null;
  }

  /**
   * Checks the text of a named label.
   *
   * @param panel the panel to inspect
   * @param name the expected component name
   * @param expected the expected label text
   */
  private void assertLabelValue(
          LongestRoadPanel panel, String name, String expected) {
    Component component = findNamedComponent(panel, name);

    assertNotNull(component);
    assertTrue(component instanceof JLabel);
    assertEquals(expected, ((JLabel) component).getText());
  }

  /**
   * Checks all displayed Longest Road information.
   *
   * @param panel the panel to inspect
   * @param holder the expected holder
   * @param length the expected road length
   * @param bonus the expected victory point bonus
   */
  private void assertLongestRoadStatus(
          LongestRoadPanel panel, String holder, String length, String bonus) {
    assertLabelValue(panel, "longest-road-holder", holder);
    assertLabelValue(panel, "longest-road-length", length);
    assertLabelValue(panel, "longest-road-bonus", bonus);
  }

  /**
   * Waits for pending Swing GUI updates to complete.
   *
   * @throws Exception if the event dispatch thread operation fails
   */
  private void flushSwingEvents() throws Exception {
    SwingUtilities.invokeAndWait(() -> {
      // Wait for pending GUI updates.
    });
  }

  @Test
  void initialStateShowsNoHolder() {
    LongestRoadPanel panel =
            new LongestRoadPanel(new LongestRoadViewModel());

    assertLongestRoadStatus(panel, "None", "-", "0 VP");
  }

  @Test
  void updateDisplaysHolderInformation() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    viewModel.updateState("Alice", 6);
    flushSwingEvents();

    assertLongestRoadStatus(panel, "Alice", "6", "+2 VP");
  }

  @Test
  void updateWithNullHolderShowsNoHolder() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    final LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    viewModel.updateState("Alice", 6);
    flushSwingEvents();

    viewModel.updateState(null, 0);
    flushSwingEvents();

    assertLongestRoadStatus(panel, "None", "-", "0 VP");
  }

  @Test
  void updateReplacesPreviousHolder() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();
    final LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    viewModel.updateState("Alice", 5);
    flushSwingEvents();

    viewModel.updateState("Bob", 7);
    flushSwingEvents();

    assertLongestRoadStatus(panel, "Bob", "7", "+2 VP");
  }

  @Test
  void panelContainsRequiredLabels() {
    LongestRoadPanel panel =
            new LongestRoadPanel(new LongestRoadViewModel());

    assertTrue(
            findNamedComponent(panel, "longest-road-holder") instanceof JLabel);

    assertTrue(
            findNamedComponent(panel, "longest-road-length") instanceof JLabel);

    assertTrue(
            findNamedComponent(panel, "longest-road-bonus") instanceof JLabel);
  }

  @Test
  void panelDisplaysExistingViewModelState() {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    viewModel.updateState("Alice", 6);

    LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    assertLongestRoadStatus(panel, "Alice", "6", "+2 VP");
  }

  @Test
  void presenterUpdatesPanelThroughViewModel() throws Exception {
    LongestRoadViewModel viewModel = new LongestRoadViewModel();

    LongestRoadPresenter presenter =
            new LongestRoadPresenter(viewModel);

    LongestRoadPanel panel = new LongestRoadPanel(viewModel);

    presenter.present(new LongestRoadOutputData("Bob", 7));
    flushSwingEvents();

    assertLongestRoadStatus(panel, "Bob", "7", "+2 VP");
  }

  @Test
  void panelRejectsNullViewModel() {
    assertThrows(
            NullPointerException.class,
            () -> new LongestRoadPanel(null));
  }
}
