package catan.framework.ui.swing;

import catan.interfaceadapter.controller.RestartGameController;
import catan.interfaceadapter.viewmodel.GameOverViewModel;
import catan.interfaceadapter.viewmodel.ScoreEntry;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/** The victory screen: the winner, everyone's final score, and the New Game and Exit buttons. */
public class GameOverView extends JPanel implements PropertyChangeListener {
    private final GameOverViewModel viewModel;

    private final JLabel winnerLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JPanel scoresPanel = new JPanel();
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
    private final JButton newGameButton = new JButton("New Game");
    private final JButton exitButton = new JButton("Exit to Main Menu");

    public GameOverView(GameOverViewModel viewModel, RestartGameController controller) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        Objects.requireNonNull(controller, "controller");

        setLayout(new BorderLayout(8, 16));
        setBorder(BorderFactory.createEmptyBorder(32, 32, 32, 32));

        JLabel title = new JLabel("Game Over", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 28f));
        winnerLabel.setName("winner");
        winnerLabel.setFont(winnerLabel.getFont().deriveFont(Font.BOLD, 20f));

        JPanel top = new JPanel(new BorderLayout(4, 8));
        top.add(title, BorderLayout.NORTH);
        top.add(winnerLabel, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        scoresPanel.setName("scores");
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        JPanel middle = new JPanel(new FlowLayout(FlowLayout.CENTER));
        middle.add(scoresPanel);
        add(middle, BorderLayout.CENTER);

        errorLabel.setName("error");
        errorLabel.setForeground(new Color(0xB00020));
        newGameButton.setName("newGame");
        newGameButton.addActionListener(e -> controller.newGame());
        exitButton.setName("exit");
        exitButton.addActionListener(e -> controller.exit());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        buttons.add(newGameButton);
        buttons.add(exitButton);
        JPanel bottom = new JPanel(new BorderLayout(4, 8));
        bottom.add(errorLabel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        viewModel.addPropertyChangeListener(this);
        refresh();
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (SwingUtilities.isEventDispatchThread()) {
            refresh();
        } else {
            SwingUtilities.invokeLater(this::refresh);
        }
    }

    private void refresh() {
        String winner = viewModel.getWinnerName();
        winnerLabel.setText(winner.isEmpty() ? " " : "Winner: " + winner);

        scoresPanel.removeAll();
        int row = 0;
        for (ScoreEntry entry : viewModel.getScores()) {
            JLabel label = new JLabel(scoreText(entry));
            label.setName("score" + row++);
            label.setFont(label.getFont().deriveFont(entry.winner() ? Font.BOLD : Font.PLAIN, 16f));
            scoresPanel.add(label);
        }
        scoresPanel.revalidate();
        scoresPanel.repaint();

        String error = viewModel.getErrorMessage();
        errorLabel.setText(error.isEmpty() ? " " : error);
    }

    private static String scoreText(ScoreEntry entry) {
        String points = entry.points() == 1 ? "1 point" : entry.points() + " points";
        return entry.playerName() + ": " + points + (entry.winner() ? "  (winner)" : "");
    }
}
