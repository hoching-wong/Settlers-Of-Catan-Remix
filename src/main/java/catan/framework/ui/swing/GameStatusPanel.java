package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/** Displays player scores, resources, current turn, and weather. */
public class GameStatusPanel extends JPanel implements PropertyChangeListener {
  private static final String[] RESOURCE_NAMES = {"LUMBER", "BRICK", "GRAIN", "WOOL", "ORE"};

  private final GameStatusViewModel viewModel;
  private final JLabel weatherLabel = new JLabel();
  private final JLabel bonusLabel = new JLabel();
  private final JLabel errorLabel = new JLabel();
  private final JPanel playersPanel = new JPanel();

  /**
   * Creates the game status panel and registers a listener.
   *
   * @param viewModel the game status view model
   */
  public GameStatusPanel(GameStatusViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);

    setLayout(new BorderLayout(8, 8));
    setBorder(BorderFactory.createTitledBorder("Game Status"));

    JPanel weatherPanel = new JPanel(new GridLayout(2, 1));
    weatherPanel.add(weatherLabel);
    weatherPanel.add(bonusLabel);

    add(weatherPanel, BorderLayout.NORTH);

    playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
    add(new JScrollPane(playersPanel), BorderLayout.CENTER);

    add(errorLabel, BorderLayout.SOUTH);

    viewModel.addPropertyChangeListener(this);
    refresh();
  }

  /** Updates the displayed game status. */
  private void refresh() {
    weatherLabel.setText(viewModel.getWeatherText());
    bonusLabel.setText(viewModel.getBonusText());
    errorLabel.setText(viewModel.getErrorMessage());

    playersPanel.removeAll();

    for (GameStatusViewModel.PlayerStatus player : viewModel.getPlayers()) {
      JPanel playerPanel = new JPanel(new GridLayout(0, 1));

      String name = player.playerName();

      if (name.equals(viewModel.getCurrentPlayerName())) {
        name += " (Current Turn)";
      }

      playerPanel.add(new JLabel(name));
      playerPanel.add(new JLabel("Victory Points: " + player.victoryPoints()));

      Map<String, Integer> resources = player.resources();

      for (String resource : RESOURCE_NAMES) {
        int count = resources.getOrDefault(resource, 0);
        playerPanel.add(new JLabel(resource + ": " + count));
      }

      playerPanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

      playersPanel.add(playerPanel);
    }

    playersPanel.revalidate();
    playersPanel.repaint();
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (javax.swing.SwingUtilities.isEventDispatchThread()) {
      refresh();
    } else {
      javax.swing.SwingUtilities.invokeLater(this::refresh);
    }
  }
}
