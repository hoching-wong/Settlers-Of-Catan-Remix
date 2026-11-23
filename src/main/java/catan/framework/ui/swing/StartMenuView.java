package catan.framework.ui.swing;

import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * The start menu: number of players, their names, the city and the Start Game button.
 * It reads {@link StartMenuViewModel} and calls {@link StartGameController}. It never touches
 * the use cases or entities.
 */
public class StartMenuView extends JPanel implements PropertyChangeListener {
    static final int MIN_PLAYERS = 2;
    static final int MAX_PLAYERS = 4;
    static final String START_TEXT = "Start Game";
    static final String STARTING_TEXT = "Starting...";

    private final StartMenuViewModel viewModel;
    private final StartGameController controller;
    private final Executor backgroundExecutor;

    private final JComboBox<Integer> playerCountBox = new JComboBox<>();
    private final JLabel[] nameLabels = new JLabel[MAX_PLAYERS];
    private final JTextField[] nameFields = new JTextField[MAX_PLAYERS];
    private final JComboBox<String> cityBox = new JComboBox<>();
    private final JButton startButton = new JButton(START_TEXT);
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);

    /**
     * @param backgroundExecutor runs the game start off the Swing thread, because it makes a
     *     network request for the weather. Tests pass a direct executor.
     */
    public StartMenuView(StartMenuViewModel viewModel, StartGameController controller,
                         Executor backgroundExecutor) {
        this.viewModel = Objects.requireNonNull(viewModel, "viewModel");
        this.controller = Objects.requireNonNull(controller, "controller");
        this.backgroundExecutor = Objects.requireNonNull(backgroundExecutor, "backgroundExecutor");
        buildComponents();
        buildLayout();
        viewModel.addPropertyChangeListener(this);
        showError();
    }

    private void buildComponents() {
        playerCountBox.setName("playerCount");
        for (int n = MIN_PLAYERS; n <= MAX_PLAYERS; n++) {
            playerCountBox.addItem(n);
        }
        playerCountBox.addActionListener(e -> updateNameFieldVisibility());

        for (int i = 0; i < MAX_PLAYERS; i++) {
            nameLabels[i] = new JLabel("Player " + (i + 1) + " name:");
            nameFields[i] = new JTextField(16);
            nameFields[i].setName("name" + (i + 1));
        }

        cityBox.setName("city");
        for (String city : viewModel.getCityNames()) {
            cityBox.addItem(city);
        }
        cityBox.setSelectedIndex(-1); // nothing chosen until the player picks a city

        startButton.setName("start");
        startButton.addActionListener(e -> startGame());

        errorLabel.setName("error");
        errorLabel.setForeground(new Color(0xB00020));
    }

    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel title = new JLabel("Settlers of Catan Remix", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(title, c);

        c.gridwidth = 1;
        c.gridy++;
        add(new JLabel("Number of players:"), c);
        c.gridx = 1;
        add(playerCountBox, c);

        for (int i = 0; i < MAX_PLAYERS; i++) {
            c.gridx = 0;
            c.gridy++;
            add(nameLabels[i], c);
            c.gridx = 1;
            add(nameFields[i], c);
        }

        c.gridx = 0;
        c.gridy++;
        add(new JLabel("City (sets the weather):"), c);
        c.gridx = 1;
        add(cityBox, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        add(errorLabel, c);

        c.gridy++;
        add(startButton, c);

        updateNameFieldVisibility();
    }

    // ---- reading the form ----

    private int selectedPlayerCount() {
        Integer count = (Integer) playerCountBox.getSelectedItem();
        return count == null ? MIN_PLAYERS : count;
    }

    /** The names typed for the players that are shown, in order. Only the shown fields count. */
    public List<String> getEnteredNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < selectedPlayerCount(); i++) {
            names.add(nameFields[i].getText());
        }
        return names;
    }

    /** The chosen city, or null if none was chosen. */
    public String getSelectedCity() {
        return (String) cityBox.getSelectedItem();
    }

    private void updateNameFieldVisibility() {
        int count = selectedPlayerCount();
        for (int i = 0; i < MAX_PLAYERS; i++) {
            boolean shown = i < count;
            nameLabels[i].setVisible(shown);
            nameFields[i].setVisible(shown);
        }
        revalidate();
        repaint();
    }

    // ---- acting ----

    private void startGame() {
        List<String> names = getEnteredNames();
        String city = getSelectedCity();
        startButton.setEnabled(false);
        startButton.setText(STARTING_TEXT);
        backgroundExecutor.execute(() -> {
            try {
                controller.startGame(names, city);
            } finally {
                SwingUtilities.invokeLater(() -> {
                    startButton.setEnabled(true);
                    startButton.setText(START_TEXT);
                });
            }
        });
    }

    // ---- reacting to the view model ----

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        // The presenter may run on a background thread, so always update on the Swing thread.
        String property = event.getPropertyName();
        if (SwingUtilities.isEventDispatchThread()) {
            handle(property);
        } else {
            SwingUtilities.invokeLater(() -> handle(property));
        }
    }

    private void handle(String property) {
        showError();
        // The menu was reset for a new game: forget what was typed for the last one.
        if (StartMenuViewModel.SUMMARY.equals(property) && viewModel.getCityName().isEmpty()) {
            clearForm();
        }
    }

    private void showError() {
        String error = viewModel.getErrorMessage();
        errorLabel.setText(error.isEmpty() ? " " : error);
    }

    private void clearForm() {
        playerCountBox.setSelectedItem(MIN_PLAYERS);
        for (JTextField field : nameFields) {
            field.setText("");
        }
        cityBox.setSelectedIndex(-1);
    }
}
