package catan.framework.ui.swing;

import catan.interfaceadapter.controller.GenerateBoardController;
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
 * It reads {@link StartMenuViewModel} and calls {@link StartGameController}.
 * It never touches the use cases or entities.
 */
public class StartMenuView extends JPanel implements PropertyChangeListener {

    static final int MIN_PLAYERS = 2;
    static final int MAX_PLAYERS = 4;
    static final String START_TEXT = "Start Game";
    static final String STARTING_TEXT = "Starting...";

    private final StartMenuViewModel viewModel;
    private final StartGameController controller;
    private final Executor backgroundExecutor;
    private GenerateBoardController generateBoardController;

    private final JComboBox<Integer> playerCountBox = new JComboBox<>();
    private final JLabel[] nameLabels = new JLabel[MAX_PLAYERS];
    private final JTextField[] nameFields = new JTextField[MAX_PLAYERS];

    private final WeatherSelectionPanel weatherPanel;

    private final JButton startButton = new JButton(START_TEXT);
    private final JLabel errorLabel =
            new JLabel(" ", SwingConstants.CENTER);

    /**
     * Constructs the start menu.
     *
     * @param viewModel the start menu view model
     * @param controller the start game controller
     * @param backgroundExecutor runs game startup off the Swing thread
     */
    public StartMenuView(
            StartMenuViewModel viewModel,
            StartGameController controller,
            Executor backgroundExecutor) {

        this.viewModel = Objects.requireNonNull(
                viewModel, "viewModel");
        this.controller = Objects.requireNonNull(
                controller, "controller");
        this.backgroundExecutor = Objects.requireNonNull(
                backgroundExecutor, "backgroundExecutor");

        weatherPanel = new WeatherSelectionPanel(
                viewModel.getCityNames());

        buildComponents();
        buildLayout();

        viewModel.addPropertyChangeListener(this);
        showError();
    }

    /**
     * Initializes the player inputs and start button.
     */
    private void buildComponents() {
        playerCountBox.setName("playerCount");

        for (int n = MIN_PLAYERS; n <= MAX_PLAYERS; n++) {
            playerCountBox.addItem(n);
        }

        playerCountBox.addActionListener(
                event -> updateNameFieldVisibility());

        for (int i = 0; i < MAX_PLAYERS; i++) {
            nameLabels[i] = new JLabel(
                    "Player " + (i + 1) + " name:");

            nameFields[i] = new JTextField(16);
            nameFields[i].setName("name" + (i + 1));
        }

        startButton.setName("start");
        startButton.addActionListener(event -> startGame());

        errorLabel.setName("startMenuError");
        errorLabel.setForeground(new Color(0xB00020));
    }

    /**
     * Arranges the components in the start menu.
     */
    private void buildLayout() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(
                24, 32, 24, 32));

        JLabel title = new JLabel(
                "Settlers of Catan Remix",
                SwingConstants.CENTER);

        title.setFont(title.getFont().deriveFont(
                Font.BOLD, 24f));

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
        add(weatherPanel, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;

        add(errorLabel, c);

        c.gridy++;
        add(startButton, c);

        updateNameFieldVisibility();
    }

    /**
     * Returns the number of selected players.
     *
     * @return the selected player count
     */
    private int selectedPlayerCount() {
        Integer count =
                (Integer) playerCountBox.getSelectedItem();

        return count == null ? MIN_PLAYERS : count;
    }

    /**
     * Returns the names entered for the selected players.
     *
     * @return the player names in order
     */
    public List<String> getEnteredNames() {
        List<String> names = new ArrayList<>();

        for (int i = 0; i < selectedPlayerCount(); i++) {
            names.add(nameFields[i].getText());
        }

        return names;
    }

    /**
     * Returns the city selected in the weather panel.
     *
     * @return the selected city, or null if none is selected
     */
    public String getSelectedCity() {
        return weatherPanel.getSelectedCity();
    }

    /**
     * Shows only the name fields needed for the selected players.
     */
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

    /**
     * Start Game first generates a new board (use case 8), then starts the game on it.
     * Without this controller the start game use case makes the board itself.
     */
    public void setGenerateBoardController(GenerateBoardController generateBoardController) {
        this.generateBoardController = generateBoardController;
    }

    /**
     * Starts a game using the selected players and city.
     */
    private void startGame() {
        List<String> names = getEnteredNames();
        String city = getSelectedCity();

        if (generateBoardController != null) {
            generateBoardController.execute();
        }
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

    /**
     * Responds to changes in the start menu view model.
     *
     * @param event the property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        String property = event.getPropertyName();

        if (SwingUtilities.isEventDispatchThread()) {
            handle(property);
        } else {
            SwingUtilities.invokeLater(
                    () -> handle(property));
        }
    }

    /**
     * Handles view model updates.
     *
     * @param property the property that changed
     */
    private void handle(String property) {
        showError();

        if (StartMenuViewModel.SUMMARY.equals(property)
                && viewModel.getCityName().isEmpty()) {
            clearForm();
        }
    }

    /**
     * Displays the current error message.
     */
    private void showError() {
        String error = viewModel.getErrorMessage();

        errorLabel.setText(
                error.isEmpty() ? " " : error);
    }

    /**
     * Clears the player names and selected city.
     */
    private void clearForm() {
        playerCountBox.setSelectedItem(MIN_PLAYERS);

        for (JTextField field : nameFields) {
            field.setText("");
        }

        weatherPanel.clearSelection();
    }
}