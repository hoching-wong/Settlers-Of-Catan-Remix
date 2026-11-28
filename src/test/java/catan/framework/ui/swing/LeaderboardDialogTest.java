package catan.framework.ui.swing;

import static catan.framework.ui.swing.SwingTestSupport.find;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import catan.interfaceadapter.viewmodel.LeaderboardViewModel;
import java.awt.GraphicsEnvironment;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Builds the dialog but never opens it. Skipped where there is no display. */
class LeaderboardDialogTest {

  private final LeaderboardPanel panel = new LeaderboardPanel(new LeaderboardViewModel());

  @BeforeEach
  void needsADisplay() {
    assumeFalse(GraphicsEnvironment.isHeadless());
  }

  @Test
  void isAModalDialogTitledLeaderboardHoldingThePanel() {
    LeaderboardDialog dialog = new LeaderboardDialog(null, panel);

    assertEquals("Leaderboard", dialog.getTitle());
    assertTrue(dialog.isModal());
    assertSame(dialog, SwingUtilities.getWindowAncestor(panel));
    dialog.dispose();
  }

  @Test
  void theCloseButtonDisposesTheDialog() {
    LeaderboardDialog dialog = new LeaderboardDialog(null, panel);
    JButton close = find(dialog.getContentPane(), "close", JButton.class);

    assertNotNull(close);
    close.doClick();

    assertFalse(dialog.isDisplayable());
  }
}
