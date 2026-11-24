package catan.framework.ui.swing;

import catan.interfaceadapter.controller.EndTurnController;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.interfaceadapter.viewmodel.LongestRoadViewModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * The game screen: whose turn it is and what they should do, the weather and its bonus, the
 * latest message, and the End Turn button. The board goes in the middle once there is a board
 * view: see {@link #setBoardComponent(JComponent)}.
 */
public class GameView extends JPanel implements PropertyChangeListener {
    private final GameViewModel viewModel;
    private final EndTurnController endTurnController;
    private final LongestRoadPanel longestRoadPanel;

    private final JLabel turnLabel = new JLabel(" ");
    private final JLabel instructionLabel = new JLabel(" ");
    private final JLabel playersLabel = new JLabel(" ");
    private final JLabel weatherLabel = new JLabel(" ");
    private final JLabel messageLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton endTurnButton = new JButton("End Turn");
    private final JPanel boardSlot = new JPanel(new BorderLayout());

    public GameView(GameViewModel viewModel, EndTurnController endTurnController) {
        this(viewModel, endTurnController, null);
    }

    /** Creates a game view with a Longest Road status panel. */
    public GameView(GameViewModel viewModel, EndTurnController endTurnController,
                    LongestRoadViewModel longestRoadViewModel) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.endTurnController = Objects.requireNonNull(endTurnController, "endTurnController");
        this.longestRoadPanel = longestRoadViewModel == null
                ? null : new LongestRoadPanel(longestRoadViewModel);
        buildLayout();
        viewModel.addPropertyChangeListener(this);
        refresh();
    }

    private void buildLayout() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        turnLabel.setName("turn");
        turnLabel.setFont(turnLabel.getFont().deriveFont(Font.BOLD, 22f));
        instructionLabel.setName("instruction");
        instructionLabel.setFont(instructionLabel.getFont().deriveFont(Font.PLAIN, 16f));
        playersLabel.setName("players");
        weatherLabel.setName("weather");
        messageLabel.setName("message");
        errorLabel.setName("gameError");
        errorLabel.setForeground(new Color(0xB00020));
        endTurnButton.setName("endTurn");
        endTurnButton.addActionListener(e -> endTurnController.endTurn());

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(turnLabel);
        header.add(instructionLabel);
        header.add(playersLabel);
        header.add(weatherLabel);
        add(header, BorderLayout.NORTH);

        JLabel placeholder = new JLabel("The board will appear here", SwingConstants.CENTER);
        placeholder.setName("boardPlaceholder");
        boardSlot.setName("boardSlot");
        boardSlot.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        boardSlot.add(placeholder, BorderLayout.CENTER);
        add(boardSlot, BorderLayout.CENTER);

        if (longestRoadPanel != null) {
            longestRoadPanel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 8));
            add(longestRoadPanel, BorderLayout.EAST);
        }

        JPanel footer = new JPanel();
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        errorLabel.setAlignmentX(CENTER_ALIGNMENT);
        endTurnButton.setAlignmentX(CENTER_ALIGNMENT);
        footer.add(messageLabel);
        footer.add(errorLabel);
        footer.add(endTurnButton);
        add(footer, BorderLayout.SOUTH);
    }

    /** Puts the board view in the middle of the screen, in place of the placeholder. */
    public void setBoardComponent(JComponent board) {
        Objects.requireNonNull(board, "board");
        boardSlot.removeAll();
        boardSlot.add(board, BorderLayout.CENTER);
        boardSlot.revalidate();
        boardSlot.repaint();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // Presenters may run on a background thread, so always update on the Swing thread.
        if (SwingUtilities.isEventDispatchThread()) {
            refresh();
        } else {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private void refresh() {
        String current = viewModel.getCurrentPlayerName();
        turnLabel.setText(current.isEmpty() ? " " : "Current turn: " + current);
        instructionLabel.setText(orSpace(viewModel.getInstruction()));
        playersLabel.setText(viewModel.getPlayerNames().isEmpty()
                ? " " : "Players: " + String.join(", ", viewModel.getPlayerNames()));
        weatherLabel.setText(viewModel.getWeatherText().isEmpty()
                ? " " : viewModel.getWeatherText() + "   |   " + viewModel.getBonusText());
        messageLabel.setText(orSpace(viewModel.getMessage()));
        errorLabel.setText(orSpace(viewModel.getErrorMessage()));
    }

    private static String orSpace(String text) {
        return text.isEmpty() ? " " : text;
    }
}
