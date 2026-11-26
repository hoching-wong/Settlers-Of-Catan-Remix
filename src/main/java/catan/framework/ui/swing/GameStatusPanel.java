package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.GameStatusViewModel;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Displays player scores, resources, current turn, city, and weather. */
public class GameStatusPanel extends JPanel implements PropertyChangeListener {
  private static final String[] RESOURCE_NAMES = {"LUMBER", "BRICK", "GRAIN", "WOOL", "ORE"};

  private static final Color PANEL_COLOR = new Color(0xF7F5F0);
  private static final Color TITLE_COLOR = new Color(0x35536B);
  private static final Color TEXT_COLOR = new Color(0x333333);
  private static final Color SECONDARY_TEXT_COLOR = new Color(0x555555);
  private static final Color WEATHER_HEADING_COLOR = new Color(0x7A6F5C);
  private static final Color POINTS_COLOR = new Color(0x2E4D66);
  private static final Color BORDER_COLOR = new Color(0xE6DED0);
  private static final Color SEPARATOR_COLOR = new Color(0xDCD7CD);
  private static final Color NO_BONUS_COLOR = new Color(0xF1EEE8);
  private static final Color TROPHY_COLOR = new Color(0xB4893E);

  private static final Color[] PLAYER_HEADER_COLORS = {
    new Color(0xE7BDC8), new Color(0xC9DDF2), new Color(0xEBCFAE), new Color(0xC9DDCC)
  };

  private static final Color[] PLAYER_ACCENT_COLORS = {
    new Color(0xB45B72), new Color(0x5686B2), new Color(0xBF8342), new Color(0x568C65)
  };

  private static final Map<String, Color> RESOURCE_COLORS =
      Map.of(
          "LUMBER", new Color(0xE8F1DD),
          "BRICK", new Color(0xF1D3C8),
          "GRAIN", new Color(0xF8EAB6),
          "WOOL", new Color(0xF0F1F2),
          "ORE", new Color(0xE6DDF1));

  private static final Map<String, Color> RESOURCE_ICON_COLORS =
      Map.of(
          "LUMBER", new Color(0x5A7440),
          "BRICK", new Color(0x985B45),
          "GRAIN", new Color(0xA88525),
          "WOOL", new Color(0x858B92),
          "ORE", new Color(0x7A6798));

  private final GameStatusViewModel viewModel;
  private final StartMenuViewModel startMenuViewModel;
  private final JLabel cityLabel = new JLabel();
  private final JLabel weatherLabel = new JLabel();
  private final JLabel weatherTitle = new JLabel("Weather");
  private final JLabel bonusLabel = new JLabel();
  private final RoundedPanel bonusBlock = new RoundedPanel(10, null);
  private final JLabel errorLabel = new JLabel();
  private final JPanel playersPanel = new JPanel();

  /**
   * Creates the game status panel and registers listeners.
   *
   * @param viewModel the game status view model
   * @param startMenuViewModel the start menu view model
   */
  public GameStatusPanel(GameStatusViewModel viewModel, StartMenuViewModel startMenuViewModel) {

    this.viewModel = Objects.requireNonNull(viewModel);
    this.startMenuViewModel = Objects.requireNonNull(startMenuViewModel);

    setLayout(new BorderLayout(0, 8));
    setBackground(PANEL_COLOR);
    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

    JPanel overviewPanel = new JPanel(new BorderLayout(0, 8));
    overviewPanel.setOpaque(false);

    overviewPanel.add(createTitlePanel(), BorderLayout.NORTH);
    overviewPanel.add(createWeatherPanel(), BorderLayout.CENTER);

    JLabel playersLabel = new JLabel("Players");

    styleLabel(playersLabel, new Color(0x5E5547), Font.BOLD, 15f);

    playersLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 0, 0));

    overviewPanel.add(playersLabel, BorderLayout.SOUTH);

    add(overviewPanel, BorderLayout.NORTH);

    playersPanel.setLayout(new BoxLayout(playersPanel, BoxLayout.Y_AXIS));

    playersPanel.setBackground(PANEL_COLOR);

    JScrollPane scrollPane = new JScrollPane(playersPanel);

    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.getViewport().setBackground(PANEL_COLOR);

    scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    scrollPane.getVerticalScrollBar().setUnitIncrement(16);

    add(scrollPane, BorderLayout.CENTER);

    errorLabel.setForeground(new Color(0xB00020));

    add(errorLabel, BorderLayout.SOUTH);

    viewModel.addPropertyChangeListener(this);
    startMenuViewModel.addPropertyChangeListener(this);

    refresh();
  }

  /** Creates the game overview title. */
  private JPanel createTitlePanel() {
    RoundedPanel panel = new RoundedPanel(12, null);

    panel.setLayout(new BorderLayout());
    panel.setBackground(TITLE_COLOR);

    panel.setBorder(BorderFactory.createEmptyBorder(9, 6, 9, 6));

    JLabel label = new JLabel("Game Overview", SwingConstants.CENTER);

    styleLabel(label, Color.WHITE, Font.BOLD, 20f);

    panel.add(label, BorderLayout.CENTER);

    return panel;
  }

  /** Creates the compact city, weather, and bonus panel. */
  private JPanel createWeatherPanel() {
    RoundedPanel panel = new RoundedPanel(12, BORDER_COLOR);

    panel.setLayout(new BorderLayout(0, 7));
    panel.setBackground(Color.WHITE);

    panel.setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

    JPanel top = new JPanel(new GridLayout(2, 2, 6, 4));
    top.setOpaque(false);

    JLabel cityTitle = new JLabel("City");

    cityTitle.setIcon(new LocationIcon());

    styleLabel(cityTitle, WEATHER_HEADING_COLOR, Font.BOLD, 13f);

    cityTitle.setIconTextGap(5);

    styleLabel(weatherTitle, WEATHER_HEADING_COLOR, Font.BOLD, 13f);

    weatherTitle.setIconTextGap(5);

    styleLabel(cityLabel, TEXT_COLOR, Font.PLAIN, 14f);
    styleLabel(weatherLabel, TEXT_COLOR, Font.PLAIN, 14f);

    cityLabel.setHorizontalAlignment(SwingConstants.LEFT);
    weatherLabel.setHorizontalAlignment(SwingConstants.LEFT);

    top.add(cityTitle);
    top.add(cityLabel);
    top.add(weatherTitle);
    top.add(weatherLabel);

    panel.add(top, BorderLayout.NORTH);

    // Separates the weather information from the weather bonus.
    panel.add(createSeparator(), BorderLayout.CENTER);

    JPanel bonusRow = new JPanel(new BorderLayout(4, 0));
    bonusRow.setOpaque(false);

    JLabel title = new JLabel("Weather Bonus");

    styleLabel(title, WEATHER_HEADING_COLOR, Font.BOLD, 13f);

    bonusRow.add(title, BorderLayout.WEST);

    bonusBlock.setBackground(NO_BONUS_COLOR);
    bonusBlock.setLayout(new BorderLayout());

    bonusBlock.setBorder(BorderFactory.createEmptyBorder(4, 3, 4, 3));

    styleLabel(bonusLabel, TITLE_COLOR, Font.BOLD, 14f);

    bonusLabel.setHorizontalAlignment(SwingConstants.CENTER);
    bonusLabel.setIconTextGap(3);

    bonusBlock.add(bonusLabel, BorderLayout.CENTER);
    bonusRow.add(bonusBlock, BorderLayout.EAST);

    panel.add(bonusRow, BorderLayout.SOUTH);

    return panel;
  }

  /**
   * Creates a visible horizontal separator.
   *
   * @return the separator panel
   */
  private JPanel createSeparator() {
    JPanel separator = new JPanel();

    separator.setOpaque(true);
    separator.setBackground(SEPARATOR_COLOR);
    separator.setPreferredSize(new Dimension(1, 1));
    separator.setMinimumSize(new Dimension(0, 1));
    separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

    return separator;
  }

  /** Updates the displayed game status. */
  private void refresh() {
    cityLabel.setText(startMenuViewModel.getCityName());

    String weather = viewModel.getWeatherText().replaceFirst("^Weather: ", "");

    if (weather.isBlank() || "None".equals(weather)) {
      weather = "Unavailable";
    }

    weatherLabel.setText(weather);
    weatherTitle.setIcon(new WeatherIcon(weather));

    updateWeatherBonus();

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

  /** Updates the bonus label using the current resource colours and icons. */
  private void updateWeatherBonus() {
    String bonus = viewModel.getBonusText().replaceFirst("^Bonus: ", "");

    if (bonus.isBlank() || "None".equals(bonus)) {
      bonusLabel.setText("No Bonus");
      bonusLabel.setForeground(SECONDARY_TEXT_COLOR);
      bonusLabel.setIcon(null);

      bonusBlock.setBackground(NO_BONUS_COLOR);
    } else {
      String resource = bonus.split("\\s+", 2)[0].toUpperCase(Locale.ROOT);

      bonusLabel.setText(bonus.replace(" x", " ×"));
      bonusLabel.setForeground(TITLE_COLOR);

      bonusBlock.setBackground(RESOURCE_COLORS.getOrDefault(resource, NO_BONUS_COLOR));

      bonusLabel.setIcon(RESOURCE_COLORS.containsKey(resource) ? new ResourceIcon(resource) : null);
    }

    bonusBlock.revalidate();
    bonusBlock.repaint();
  }

  /**
   * Creates a rounded player card with a coloured accent strip.
   *
   * @param player the player's status
   * @param playerIndex the player's index
   * @return the player card
   */
  private JPanel createPlayerCard(GameStatusViewModel.PlayerStatus player, int playerIndex) {

    final Color headerColor = PLAYER_HEADER_COLORS[playerIndex];
    final Color accentColor = PLAYER_ACCENT_COLORS[playerIndex];

    boolean currentPlayer = player.playerName().equals(viewModel.getCurrentPlayerName());

    RoundedPanel card = new RoundedPanel(14, currentPlayer ? accentColor : BORDER_COLOR);

    card.setName("player-card-" + player.playerName());
    card.setBackground(Color.WHITE);
    card.setLayout(new BorderLayout());

    card.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

    // A thin coloured strip identifies each player.
    JPanel colourStrip = new JPanel();

    colourStrip.setBackground(headerColor);
    colourStrip.setPreferredSize(new Dimension(0, 5));

    // The player name and current turn label use a white background.
    JPanel header = new JPanel(new BorderLayout(5, 0));

    header.setOpaque(false);

    header.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

    JLabel nameLabel = new JLabel(player.playerName());

    styleLabel(nameLabel, TEXT_COLOR, Font.BOLD, 16f);

    nameLabel.setIcon(new DotIcon(accentColor));
    nameLabel.setIconTextGap(7);
    nameLabel.setToolTipText(player.playerName());

    header.add(nameLabel, BorderLayout.CENTER);

    if (currentPlayer) {
      RoundedPanel turnBlock = new RoundedPanel(18, null);

      turnBlock.setBackground(TITLE_COLOR);
      turnBlock.setLayout(new BorderLayout());

      turnBlock.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

      JLabel turnLabel = new JLabel("Current Turn");

      styleLabel(turnLabel, Color.WHITE, Font.BOLD, 11f);

      turnBlock.add(turnLabel, BorderLayout.CENTER);
      header.add(turnBlock, BorderLayout.EAST);
    }

    JPanel headerSection = new JPanel(new BorderLayout());

    headerSection.setOpaque(false);

    headerSection.add(colourStrip, BorderLayout.NORTH);
    headerSection.add(header, BorderLayout.CENTER);

    card.add(headerSection, BorderLayout.NORTH);

    JPanel details = new JPanel(new BorderLayout(0, 10));

    details.setOpaque(false);

    details.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

    // Victory Points remains on a white background.
    JPanel pointsPanel = new JPanel(new BorderLayout(4, 0));

    pointsPanel.setOpaque(false);

    JLabel pointsLabel = new JLabel("Victory Points");

    styleLabel(pointsLabel, new Color(0x4A4A4A), Font.BOLD, 14f);

    pointsLabel.setIcon(new TrophyIcon());
    pointsLabel.setIconTextGap(7);

    JLabel pointsValue = new JLabel(String.valueOf(player.victoryPoints()));

    pointsValue.setName("victory-points-value");

    styleLabel(pointsValue, POINTS_COLOR, Font.BOLD, 20f);

    pointsPanel.add(pointsLabel, BorderLayout.WEST);
    pointsPanel.add(pointsValue, BorderLayout.EAST);

    // Separates the victory points from the resource blocks.
    JPanel pointsSection = new JPanel(new BorderLayout(0, 3));

    pointsSection.setOpaque(false);

    pointsSection.add(pointsPanel, BorderLayout.NORTH);
    pointsSection.add(createSeparator(), BorderLayout.SOUTH);

    details.add(pointsSection, BorderLayout.NORTH);

    // Displays the five resources in two columns.
    JPanel resourcePanel = new JPanel(new GridLayout(0, 2, 5, 5));

    resourcePanel.setOpaque(false);

    for (String resource : RESOURCE_NAMES) {
      int count = player.resources().getOrDefault(resource, 0);

      resourcePanel.add(createResourceBlock(resource, count));
    }

    details.add(resourcePanel, BorderLayout.CENTER);

    card.add(details, BorderLayout.CENTER);

    card.setAlignmentX(LEFT_ALIGNMENT);

    card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));

    return card;
  }

  /**
   * Creates a resource block with a left-aligned name and a right-aligned count.
   *
   * @param resource the resource name
   * @param count the resource count
   * @return the resource block
   */
  private JPanel createResourceBlock(String resource, int count) {
    RoundedPanel block = new RoundedPanel(9, null);

    block.setName("resource-" + resource);

    block.setBackground(RESOURCE_COLORS.get(resource));

    block.setLayout(new BorderLayout(2, 0));

    block.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));

    String displayName = resource.substring(0, 1) + resource.substring(1).toLowerCase(Locale.ROOT);

    JLabel label = new JLabel(displayName);

    styleLabel(label, TEXT_COLOR, Font.PLAIN, 13f);

    label.setIcon(new ResourceIcon(resource));
    label.setIconTextGap(3);

    JLabel countLabel = new JLabel(String.valueOf(count));

    countLabel.setName("resource-count");

    styleLabel(countLabel, TEXT_COLOR, Font.BOLD, 14f);

    block.add(label, BorderLayout.CENTER);
    block.add(countLabel, BorderLayout.EAST);

    block.setToolTipText(displayName + ": " + count);

    return block;
  }

  /** Applies consistent font and colour settings to a label. */
  private static void styleLabel(JLabel label, Color colour, int style, float size) {

    label.setForeground(colour);
    label.setFont(label.getFont().deriveFont(style, size));
  }

  /** A panel with a rounded background and an optional outline. */
  private static class RoundedPanel extends JPanel {
    private final int arc;
    private final Color outline;

    RoundedPanel(int arc, Color outline) {
      this.arc = arc;
      this.outline = outline;

      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D drawing = (Graphics2D) graphics.create();

      drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      drawing.setColor(getBackground());

      drawing.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

      drawing.dispose();
    }

    @Override
    protected void paintChildren(Graphics graphics) {
      Graphics2D drawing = (Graphics2D) graphics.create();

      drawing.clip(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), arc, arc));

      super.paintChildren(drawing);

      drawing.dispose();

      if (outline != null) {
        Graphics2D border = (Graphics2D) graphics.create();

        border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        border.setColor(outline);

        border.draw(
            new RoundRectangle2D.Double(0.5, 0.5, getWidth() - 1.0, getHeight() - 1.0, arc, arc));

        border.dispose();
      }
    }
  }

  /** A small coloured marker identifying a player. */
  private static class DotIcon implements Icon {
    private static final int ICON_SIZE = 10;

    private final Color colour;

    DotIcon(Color colour) {
      this.colour = colour;
    }

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      Graphics2D drawing = (Graphics2D) graphics.create();

      drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      drawing.setColor(colour);

      drawing.fillOval(x, y, getIconWidth(), getIconHeight());

      drawing.dispose();
    }
  }

  /** Creates a local, antialiased drawing context for an icon. */
  private static Graphics2D iconGraphics(Graphics graphics, int x, int y, float stroke) {

    Graphics2D drawing = (Graphics2D) graphics.create();

    drawing.translate(x, y);

    drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    drawing.setStroke(new BasicStroke(stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    return drawing;
  }

  /** An icon representing the selected city. */
  private static class LocationIcon implements Icon {
    private static final int ICON_SIZE = 22;

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      Graphics2D drawing = iconGraphics(graphics, x, y, 1.6f);

      drawing.scale(22.0 / 18.0, 22.0 / 18.0);
      drawing.setColor(TITLE_COLOR);

      Path2D pin = new Path2D.Double();

      pin.moveTo(9, 16);
      pin.curveTo(6, 12, 3, 10, 3, 6.8);
      pin.curveTo(3, 3.6, 5.7, 1.5, 9, 1.5);
      pin.curveTo(12.3, 1.5, 15, 3.6, 15, 6.8);
      pin.curveTo(15, 10, 12, 12, 9, 16);
      pin.closePath();

      drawing.draw(pin);
      drawing.drawOval(7, 5, 4, 4);

      drawing.dispose();
    }
  }

  /** A trophy icon representing victory points. */
  private static class TrophyIcon implements Icon {
    private static final int ICON_SIZE = 20;

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      Graphics2D drawing = iconGraphics(graphics, x, y, 1.4f);

      drawing.scale(20.0 / 18.0, 20.0 / 18.0);

      drawing.setColor(TROPHY_COLOR);

      Path2D cup = new Path2D.Double();

      cup.moveTo(5, 2);
      cup.lineTo(13, 2);
      cup.lineTo(13, 6);
      cup.curveTo(13, 11, 5, 11, 5, 6);
      cup.closePath();

      drawing.draw(cup);

      Path2D handles = new Path2D.Double();

      handles.moveTo(5, 4);
      handles.lineTo(2, 4);
      handles.curveTo(2, 8, 4, 9, 6, 8);

      handles.moveTo(13, 4);
      handles.lineTo(16, 4);
      handles.curveTo(16, 8, 14, 9, 12, 8);

      drawing.draw(handles);

      drawing.drawLine(9, 10, 9, 15);
      drawing.drawLine(5, 15, 13, 15);

      drawing.dispose();
    }
  }

  /** An icon representing the current weather condition. */
  private static class WeatherIcon implements Icon {
    private static final int ICON_SIZE = 22;
    private static final int DRAWING_SIZE = 32;

    private final String weather;

    WeatherIcon(String weather) {
      this.weather = weather;
    }

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      Graphics2D drawing = iconGraphics(graphics, x, y, 2f);

      double scale = (double) ICON_SIZE / DRAWING_SIZE;

      drawing.scale(scale, scale);

      switch (weather) {
        case "Sunny" -> drawSun(drawing);

        case "Cloudy" -> {
          drawing.setColor(new Color(0x8594A5));
          drawing.translate(0, 4);
          drawCloud(drawing);
        }

        case "Rainy" -> {
          drawing.setColor(new Color(0x528BC0));

          drawCloud(drawing);

          drawing.drawLine(9, 23, 9, 28);
          drawing.drawLine(16, 25, 16, 30);
          drawing.drawLine(23, 23, 23, 28);
        }

        case "Snowy" -> drawSnow(drawing);

        case "Stormy" -> {
          drawing.setColor(new Color(0x79649E));

          drawCloud(drawing);

          Path2D lightning = new Path2D.Double();

          lightning.moveTo(17, 17);
          lightning.lineTo(12, 24);
          lightning.lineTo(18, 24);
          lightning.lineTo(14, 30);

          drawing.draw(lightning);
        }

        case "Fog" -> {
          drawing.setColor(new Color(0x8B9DA9));

          drawCloud(drawing);

          drawing.drawLine(4, 24, 28, 24);
          drawing.drawLine(8, 28, 24, 28);
        }

        default -> {
          drawing.setColor(new Color(0x96958D));
          drawing.translate(0, 4);

          drawCloud(drawing);

          drawing.drawLine(3, 0, 29, 26);
        }
      }

      drawing.dispose();
    }

    /** Draws the sun and its rays. */
    private static void drawSun(Graphics2D drawing) {
      drawing.setColor(new Color(0xDDA329));

      drawing.drawOval(10, 10, 12, 12);

      for (int index = 0; index < 8; index++) {
        double angle = index * Math.PI / 4;

        int startX = (int) Math.round(16 + 10 * Math.cos(angle));

        int startY = (int) Math.round(16 + 10 * Math.sin(angle));

        int endX = (int) Math.round(16 + 14 * Math.cos(angle));

        int endY = (int) Math.round(16 + 14 * Math.sin(angle));

        drawing.drawLine(startX, startY, endX, endY);
      }
    }

    /** Draws the outline of a cloud. */
    private static void drawCloud(Graphics2D drawing) {
      Path2D cloud = new Path2D.Double();

      cloud.moveTo(9, 20);

      cloud.curveTo(1, 20, 1, 10, 8, 9);
      cloud.curveTo(9, 1, 21, 1, 23, 10);
      cloud.curveTo(32, 9, 33, 20, 25, 20);

      cloud.closePath();

      drawing.draw(cloud);
    }

    /** Draws a snowflake. */
    private static void drawSnow(Graphics2D drawing) {
      drawing.setColor(new Color(0x7CAACB));

      for (int index = 0; index < 6; index++) {
        Graphics2D branch = (Graphics2D) drawing.create();

        branch.translate(16, 16);
        branch.rotate(index * Math.PI / 3);

        branch.drawLine(0, 0, 0, -13);
        branch.drawLine(0, -8, -4, -12);
        branch.drawLine(0, -8, 4, -12);

        branch.dispose();
      }
    }
  }

  /** An icon representing a resource. */
  private static class ResourceIcon implements Icon {
    private static final int ICON_SIZE = 16;

    private final String resource;

    ResourceIcon(String resource) {
      this.resource = resource;
    }

    @Override
    public int getIconWidth() {
      return ICON_SIZE;
    }

    @Override
    public int getIconHeight() {
      return ICON_SIZE;
    }

    @Override
    public void paintIcon(Component component, Graphics graphics, int x, int y) {

      Graphics2D drawing = iconGraphics(graphics, x, y, 1.3f);

      drawing.setColor(RESOURCE_ICON_COLORS.getOrDefault(resource, TEXT_COLOR));

      switch (resource) {
        case "LUMBER" -> drawLumber(drawing);
        case "BRICK" -> drawBrick(drawing);
        case "GRAIN" -> drawGrain(drawing);
        case "WOOL" -> drawWool(drawing);
        case "ORE" -> drawOre(drawing);

        default -> {
          // No icon is drawn for an unknown resource.
        }
      }

      drawing.dispose();
    }

    /** Draws two trees representing lumber. */
    private static void drawLumber(Graphics2D drawing) {
      drawing.drawPolygon(new int[] {5, 1, 9}, new int[] {2, 11, 11}, 3);

      drawing.drawLine(5, 11, 5, 14);

      drawing.drawPolygon(new int[] {11, 8, 14}, new int[] {5, 12, 12}, 3);

      drawing.drawLine(11, 12, 11, 14);
    }

    /** Draws a brick wall. */
    private static void drawBrick(Graphics2D drawing) {
      drawing.drawRect(1, 3, 13, 10);

      drawing.drawLine(1, 8, 14, 8);

      drawing.drawLine(5, 3, 5, 8);
      drawing.drawLine(10, 3, 10, 8);

      drawing.drawLine(8, 8, 8, 13);
    }

    /** Draws a wheat stalk representing grain. */
    private static void drawGrain(Graphics2D drawing) {
      drawing.drawLine(8, 2, 8, 14);

      drawing.drawLine(8, 5, 4, 2);
      drawing.drawLine(8, 5, 12, 2);

      drawing.drawLine(8, 8, 3, 5);
      drawing.drawLine(8, 8, 13, 5);

      drawing.drawLine(8, 11, 3, 8);
      drawing.drawLine(8, 11, 13, 8);
    }

    /** Draws the wool outline. */
    private static void drawWool(Graphics2D drawing) {
      Path2D wool = new Path2D.Double();

      wool.moveTo(4, 13);
      wool.curveTo(0, 13, 0, 7, 4, 7);
      wool.curveTo(3, 2, 9, 1, 10, 5);
      wool.curveTo(15, 3, 17, 10, 13, 12);
      wool.curveTo(12, 13, 10, 13, 4, 13);

      wool.closePath();

      drawing.draw(wool);
    }

    /** Draws a gemstone representing ore. */
    private static void drawOre(Graphics2D drawing) {
      drawing.drawPolygon(new int[] {1, 4, 11, 14, 8}, new int[] {6, 2, 2, 6, 14}, 5);

      drawing.drawLine(1, 6, 14, 6);

      drawing.drawLine(4, 2, 8, 14);
      drawing.drawLine(11, 2, 8, 14);
    }
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
