package catan.view.longestroad;

import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Displays information about the current Longest Road holder. */
public class LongestRoadPanel extends JPanel {

  private final JLabel holderLabel = new JLabel("Current Holder: None");
  private final JLabel lengthLabel = new JLabel("Longest Road Length: -");
  private final JLabel bonusLabel = new JLabel("Victory Point Bonus: 0 VP");

  /** Constructs the Longest Road display panel. */
  public LongestRoadPanel() {
    setLayout(new GridLayout(0, 1, 0, 8));

    add(new JLabel("Longest Road"));
    add(holderLabel);
    add(lengthLabel);
    add(bonusLabel);
  }

  /**
   * Updates the displayed Longest Road information.
   *
   * @param holderName the current holder's name, or null
   * @param roadLength the current holder's longest road length
   */
  public void updateLongestRoad(String holderName, int roadLength) {
    if (holderName == null) {
      holderLabel.setText("Current Holder: None");
      lengthLabel.setText("Longest Road Length: -");
      bonusLabel.setText("Victory Point Bonus: 0 VP");
      return;
    }

    holderLabel.setText("Current Holder: " + holderName);
    lengthLabel.setText("Longest Road Length: " + roadLength);
    bonusLabel.setText("Victory Point Bonus: +2 VP");
  }
}
