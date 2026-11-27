package catan.framework.ui.swing;

import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** Displays the current Longest Road information. */
public class LongestRoadPanel extends JPanel implements PropertyChangeListener {

  private static final Color TITLE_COLOR = new Color(0x35536B);
  private static final Color TEXT_COLOR = new Color(0x333333);
  private static final Color HEADING_COLOR = new Color(0x7A6F5C);
  private static final Color POINTS_COLOR = new Color(0x2E4D66);
  private static final Color BORDER_COLOR = new Color(0xE6DED0);
  private static final Color SEPARATOR_COLOR = new Color(0xDCD7CD);
  private static final Color GOLD_COLOR = new Color(0xB4893E);
  private static final Color BONUS_COLOR = new Color(0xFBF1D4);
  private static final Color NO_BONUS_COLOR = new Color(0xF1EEE8);
  private static final Color INACTIVE_COLOR = new Color(0x96958D);

  private final JLabel holderLabel = new JLabel("None");
  private final JLabel lengthLabel = new JLabel("-");
  private final JLabel bonusLabel = new JLabel("0 VP");
  private final JLabel trophyLabel = new JLabel();
  private final RoundedPanel bonusBlock = new RoundedPanel();

  private final LongestRoadViewModel viewModel;

  /**
   * Creates a Longest Road panel connected to a view model.
   *
   * @param viewModel the view model
   */
  public LongestRoadPanel(LongestRoadViewModel viewModel) {
    this.viewModel = Objects.requireNonNull(viewModel, "viewModel");

    setName("longest-road-panel");
    setOpaque(false);
    setBackground(Color.WHITE);
    setLayout(new BorderLayout(0, 7));
    setBorder(BorderFactory.createEmptyBorder(9, 9, 9, 9));

    add(createTitlePanel(), BorderLayout.NORTH);
    add(createHolderPanel(), BorderLayout.CENTER);
    add(createLengthPanel(), BorderLayout.SOUTH);

    viewModel.addPropertyChangeListener(this);

    refreshFromViewModel();
  }

  /** Creates the Longest Road heading and its separator. */
  private JPanel createTitlePanel() {
    JPanel panel = new JPanel(new BorderLayout(0, 7));
    panel.setOpaque(false);

    JLabel title = new JLabel("Longest Road");

    styleLabel(title, TITLE_COLOR, Font.BOLD, 16f);

    title.setIcon(new ChainIcon());
    title.setIconTextGap(7);

    panel.add(title, BorderLayout.NORTH);
    panel.add(createSeparator(), BorderLayout.SOUTH);

    return panel;
  }

  /** Creates the holder name, trophy icon, and victory point bonus. */
  private JPanel createHolderPanel() {
    JPanel panel = new JPanel(new BorderLayout(8, 0));
    panel.setOpaque(false);

    trophyLabel.setIcon(new TrophyIcon(GOLD_COLOR));

    panel.add(trophyLabel, BorderLayout.WEST);

    JPanel holderDetails = new JPanel(new BorderLayout(0, 2));
    holderDetails.setOpaque(false);

    JLabel holderTitle = new JLabel("Holder");

    styleLabel(holderTitle, HEADING_COLOR, Font.BOLD, 13f);
    styleLabel(holderLabel, TEXT_COLOR, Font.BOLD, 20f);

    holderLabel.setName("longest-road-holder");

    holderDetails.add(holderTitle, BorderLayout.NORTH);
    holderDetails.add(holderLabel, BorderLayout.CENTER);

    panel.add(holderDetails, BorderLayout.CENTER);

    bonusBlock.setBackground(BONUS_COLOR);
    bonusBlock.setLayout(new BorderLayout());
    bonusBlock.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

    bonusLabel.setName("longest-road-bonus");
    bonusLabel.setHorizontalAlignment(SwingConstants.CENTER);

    styleLabel(bonusLabel, GOLD_COLOR, Font.BOLD, 18f);

    bonusBlock.add(bonusLabel, BorderLayout.CENTER);

    JPanel bonusContainer = new JPanel(new BorderLayout());
    bonusContainer.setOpaque(false);
    bonusContainer.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));

    bonusContainer.add(bonusBlock, BorderLayout.CENTER);

    panel.add(bonusContainer, BorderLayout.EAST);

    return panel;
  }

  /** Creates the road length row and its separator. */
  private JPanel createLengthPanel() {
    JPanel panel = new JPanel(new BorderLayout(0, 7));
    panel.setOpaque(false);

    panel.add(createSeparator(), BorderLayout.NORTH);

    JPanel lengthRow = new JPanel(new BorderLayout(5, 0));
    lengthRow.setOpaque(false);

    JLabel lengthTitle = new JLabel("Road Length");

    styleLabel(lengthTitle, HEADING_COLOR, Font.BOLD, 14f);
    styleLabel(lengthLabel, POINTS_COLOR, Font.BOLD, 20f);

    lengthLabel.setName("longest-road-length");
    lengthLabel.setHorizontalAlignment(SwingConstants.RIGHT);

    lengthRow.add(lengthTitle, BorderLayout.WEST);
    lengthRow.add(lengthLabel, BorderLayout.EAST);

    panel.add(lengthRow, BorderLayout.CENTER);

    return panel;
  }

  /** Creates a horizontal separator. */
  private JPanel createSeparator() {
    JPanel separator = new JPanel();

    separator.setOpaque(true);
    separator.setBackground(SEPARATOR_COLOR);
    separator.setPreferredSize(new Dimension(1, 1));

    return separator;
  }

  /** Applies consistent font and colour settings to a label. */
  private static void styleLabel(JLabel label, Color colour, int style, float size) {
    label.setForeground(colour);
    label.setFont(label.getFont().deriveFont(style, size));
  }

  /**
   * Updates the labels with the Longest Road information.
   *
   * @param holderName the holder's name, or null
   * @param roadLength the holder's road length
   */
  private void updateLongestRoad(String holderName, int roadLength) {
    if (holderName == null) {
      holderLabel.setText("None");
      holderLabel.setForeground(INACTIVE_COLOR);
      holderLabel.setToolTipText(null);

      lengthLabel.setText("-");
      lengthLabel.setForeground(INACTIVE_COLOR);

      bonusLabel.setText("0 VP");
      bonusLabel.setForeground(INACTIVE_COLOR);
      bonusBlock.setBackground(NO_BONUS_COLOR);

      trophyLabel.setIcon(new TrophyIcon(INACTIVE_COLOR));
    } else {
      holderLabel.setText(holderName);
      holderLabel.setForeground(TEXT_COLOR);
      holderLabel.setToolTipText(holderName);

      lengthLabel.setText(String.valueOf(roadLength));
      lengthLabel.setForeground(POINTS_COLOR);

      bonusLabel.setText("+2 VP");
      bonusLabel.setForeground(GOLD_COLOR);
      bonusBlock.setBackground(BONUS_COLOR);

      trophyLabel.setIcon(new TrophyIcon(GOLD_COLOR));
    }

    revalidate();
    repaint();
  }

  /** Refreshes the panel from the view model. */
  private void refreshFromViewModel() {
    updateLongestRoad(viewModel.getHolderName(), viewModel.getRoadLength());
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

  /** Draws the rounded white background and border. */
  @Override
  protected void paintComponent(Graphics graphics) {
    super.paintComponent(graphics);

    Graphics2D drawing = (Graphics2D) graphics.create();

    drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    drawing.setColor(getBackground());

    drawing.fill(new RoundRectangle2D.Double(
            0, 0, getWidth(), getHeight(), 12, 12));

    drawing.setColor(BORDER_COLOR);

    drawing.draw(new RoundRectangle2D.Double(
            0.5, 0.5, getWidth() - 1.0, getHeight() - 1.0, 12, 12));

    drawing.dispose();
  }

  /** A rounded panel used for the victory point bonus. */
  private static class RoundedPanel extends JPanel {

    RoundedPanel() {
      setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      super.paintComponent(graphics);

      Graphics2D drawing = (Graphics2D) graphics.create();

      drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
              RenderingHints.VALUE_ANTIALIAS_ON);

      drawing.setColor(getBackground());

      drawing.fill(new RoundRectangle2D.Double(
              0, 0, getWidth(), getHeight(), 10, 10));

      drawing.dispose();
    }
  }

  /** Creates an antialiased graphics context for drawing an icon. */
  private static Graphics2D iconGraphics(
          Graphics graphics, int x, int y, float stroke) {

    Graphics2D drawing = (Graphics2D) graphics.create();

    drawing.translate(x, y);

    drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

    drawing.setStroke(new BasicStroke(
            stroke, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

    return drawing;
  }

  /** A golden chain icon representing connected roads. */
  private static class ChainIcon implements Icon {
    private static final int ICON_SIZE = 24;

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

      drawing.setColor(GOLD_COLOR);

      drawing.translate(12, 12);
      drawing.rotate(-Math.PI / 4);

      drawing.draw(new RoundRectangle2D.Double(
              -10, -3.5, 9, 7, 5, 5));

      drawing.draw(new RoundRectangle2D.Double(
              1, -3.5, 9, 7, 5, 5));

      drawing.drawLine(-4, 0, 4, 0);

      drawing.dispose();
    }
  }

  /** A trophy icon representing the Longest Road holder. */
  private static class TrophyIcon implements Icon {
    private static final int ICON_SIZE = 30;

    private final Color colour;

    TrophyIcon(Color colour) {
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
      Graphics2D drawing = iconGraphics(graphics, x, y, 1.5f);

      drawing.scale(30.0 / 18.0, 30.0 / 18.0);
      drawing.setColor(colour);

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
}
