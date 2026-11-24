package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/** Displays the current Longest Road information. */
public class LongestRoadPanel extends JPanel implements PropertyChangeListener {

  private final JLabel holderLabel = new JLabel("Current Holder: None");

  private final JLabel lengthLabel = new JLabel("Longest Road Length: -");

  private final JLabel bonusLabel = new JLabel("Victory Point Bonus: 0 VP");

  private LongestRoadViewModel viewModel;

  /** Creates a Longest Road panel without a view model. */
  public LongestRoadPanel() {
    setLayout(new GridLayout(0, 1, 0, 8));

    add(new JLabel("Longest Road"));
    add(holderLabel);
    add(lengthLabel);
    add(bonusLabel);
  }

  /**
   * Creates a Longest Road panel connected to a view model.
   *
   * @param viewModel the view model
   */
  public LongestRoadPanel(LongestRoadViewModel viewModel) {
    this();

    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
    viewModel.addPropertyChangeListener(this);

    refreshFromViewModel();
  }

  /**
   * Updates the labels with the Longest Road information.
   *
   * @param holderName the holder's name, or null
   * @param roadLength the holder's road length
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

  /** Refreshes the panel from the view model. */
  private void refreshFromViewModel() {
    if (viewModel != null) {
      updateLongestRoad(viewModel.getHolderName(), viewModel.getRoadLength());
    }
  }

  /**
   * Responds to changes in the view model.
   *
   * @param event the property change event
   */
  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if ("longestRoad".equals(event.getPropertyName())) {
      if (SwingUtilities.isEventDispatchThread()) {
        refreshFromViewModel();
      } else {
        SwingUtilities.invokeLater(this::refreshFromViewModel);
      }
    }
  }
}
