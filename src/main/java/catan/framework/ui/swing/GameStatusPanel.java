package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Displays player scores, resources, current turn, city, and weather. */
public class GameStatusPanel extends JPanel implements PropertyChangeListener {
  private static final String[] RESOURCE_NAMES = {
          "LUMBER", "BRICK", "GRAIN", "WOOL", "ORE"
  };

  private static final Color PANEL_COLOR = new Color(0xF5F1E7);
  private static final Color WEATHER_COLOR = new Color(0xE3EDF6);
  private static final Color TEXT_COLOR = new Color(0x333333);

  private static final Map<String, Color> RESOURCE_COLORS = Map.of(
          "LUMBER", new Color(0xD9E8D5),
          "BRICK", new Color(0xEDD2C5),
          "GRAIN", new Color(0xF5E6B5),
          "WOOL", new Color(0xE5EFC9),
          "ORE", new Color(0xDFE2E7));

  private final GameStatusViewModel viewModel;
  private final StartMenuViewModel startMenuViewModel;
  private final JLabel cityLabel = new JLabel();
  private final JLabel weatherLabel = new JLabel();
  private final JLabel bonusLabel = new JLabel();
  private final JLabel errorLabel = new JLabel();
  private final JPanel playersPanel = new JPanel();

  /**
   * Creates the game status panel and registers listeners.
   *
   * @param viewModel the game status view model
   * @param startMenuViewModel the start menu view model
   */
  public GameStatusPanel(
          GameStatusViewModel viewModel,
          StartMenuViewModel startMenuViewModel) {
    this.viewModel = Objects.requireNonNull(viewModel);
    this.startMenuViewModel = Objects.requireNonNull(startMenuViewModel);

    setLayout(new BorderLayout(8, 8));
    setBackground(PANEL_COLOR);
    setBorder(BorderFactory.createTitledBorder("Game Status"));

    JPanel weatherPanel = new JPanel(new GridLayout(3, 1, 0, 4));
    weatherPanel.setBackground(WEATHER_COLOR);
    weatherPanel.setBorder(
            BorderFactory.createEmptyBorder(10, 10, 10, 10));

    cityLabel.setFont(cityLabel.getFont().deriveFont(Font.BOLD));
    cityLabel.setForeground(new Color(0x254B70));
    weatherLabel.setForeground(new Color(0x254B70));
    bonusLabel.setForeground(new Color(0x28733C));
    bonusLabel.setFont(bonusLabel.getFont().deriveFont(Font.BOLD));

    weatherPanel.add(cityLabel);
    weatherPanel.add(weatherLabel);
    weatherPanel.add(bonusLabel);

    add(weatherPanel, BorderLayout.NORTH);

    playersPanel.setLayout(
            new BoxLayout(playersPanel, BoxLayout.Y_AXIS));
    playersPanel.setBackground(PANEL_COLOR);

    JScrollPane scrollPane = new JScrollPane(playersPanel);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    add(scrollPane, BorderLayout.CENTER);

    errorLabel.setForeground(new Color(0xB00020));
    add(errorLabel, BorderLayout.SOUTH);

    viewModel.addPropertyChangeListener(this);
    startMenuViewModel.addPropertyChangeListener(this);
    refresh();
  }

  /** Updates the displayed game status. */
  private void refresh() {
    String cityName = startMenuViewModel.getCityName();
    cityLabel.setText(cityName.isEmpty() ? "" : "City: " + cityName);

    weatherLabel.setText(viewModel.getWeatherText());
    bonusLabel.setText(viewModel.getBonusText());
    errorLabel.setText(viewModel.getErrorMessage());

    playersPanel.removeAll();

    int playerIndex = 0;

    for (GameStatusViewModel.PlayerStatus player : viewModel.getPlayers()) {
      playersPanel.add(createPlayerCard(player, playerIndex));
      playersPanel.add(Box.createVerticalStrut(8));
      playerIndex++;
    }

    playersPanel.revalidate();
    playersPanel.repaint();
  }

  /**
   * Creates a coloured card for one player.
   *
   * @param player the player's current status
   * @param playerIndex the player's index in the game
   * @return the player's status card
   */
  private JPanel createPlayerCard(
          GameStatusViewModel.PlayerStatus player, int playerIndex) {

    Color playerColor = BoardColors.player(playerIndex);

    boolean currentPlayer =
            player.playerName().equals(viewModel.getCurrentPlayerName());

    String name = player.playerName();

    if (currentPlayer) {
      name += " (Current Turn)";
    }

    JPanel card = new JPanel(new BorderLayout());
    card.setBackground(Color.WHITE);
    card.setBorder(
            BorderFactory.createLineBorder(
                    playerColor, currentPlayer ? 2 : 1));

    JLabel nameLabel = new JLabel(name);
    nameLabel.setOpaque(true);
    nameLabel.setBackground(playerColor);
    nameLabel.setForeground(
            playerIndex == 1 ? Color.WHITE : Color.BLACK);
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
    nameLabel.setBorder(
            BorderFactory.createEmptyBorder(8, 8, 8, 8));

    card.add(nameLabel, BorderLayout.NORTH);

    JPanel details = new JPanel(new BorderLayout(0, 8));
    details.setBackground(Color.WHITE);
    details.setBorder(
            BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JLabel pointsLabel =
            new JLabel("Victory Points: " + player.victoryPoints());

    pointsLabel.setForeground(TEXT_COLOR);
    pointsLabel.setFont(
            pointsLabel.getFont().deriveFont(Font.BOLD));

    details.add(pointsLabel, BorderLayout.NORTH);

    JPanel resourcePanel = new JPanel(new GridLayout(0, 2, 4, 4));
    resourcePanel.setBackground(Color.WHITE);

    for (String resource : RESOURCE_NAMES) {
      int count = player.resources().getOrDefault(resource, 0);
      resourcePanel.add(createResourceLabel(resource, count));
    }

    details.add(resourcePanel, BorderLayout.CENTER);
    card.add(details, BorderLayout.CENTER);

    card.setAlignmentX(LEFT_ALIGNMENT);
    card.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

    return card;
  }

  /**
   * Creates a coloured resource label.
   *
   * @param resource the resource name
   * @param count the resource count
   * @return the resource label
   */
  private JLabel createResourceLabel(String resource, int count) {
    JLabel label =
            new JLabel(resource + ": " + count, SwingConstants.CENTER);

    label.setOpaque(true);
    label.setBackground(RESOURCE_COLORS.get(resource));
    label.setForeground(TEXT_COLOR);
    label.setFont(label.getFont().deriveFont(Font.BOLD));
    label.setBorder(
            BorderFactory.createEmptyBorder(6, 2, 6, 2));

    return label;
  }

  @Override
  public void propertyChange(PropertyChangeEvent event) {
    if (SwingUtilities.isEventDispatchThread()) {
      refresh();
    } else {
      SwingUtilities.invokeLater(this::refresh);
    }
  }
}