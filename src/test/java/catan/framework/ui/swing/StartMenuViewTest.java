package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.usecase.startgame.StartGameInputData;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/** Drives the real Swing components without showing a window. */
class StartMenuViewTest {
    private final StartMenuViewModel viewModel =
            new StartMenuViewModel(List.of("Toronto", "London", "Tokyo", "New York"));
    private final List<StartGameInputData> started = new ArrayList<>();
    private final StartGameController controller = new StartGameController(started::add);
    private final StartMenuView view = new StartMenuView(viewModel, controller, Runnable::run);

    // ---- helpers ----

    private static <T extends Component> T find(Container root, String name, Class<T> type) {
        for (Component c : root.getComponents()) {
            if (name.equals(c.getName()) && type.isInstance(c)) {
                return type.cast(c);
            }
            if (c instanceof Container inner) {
                T found = find(inner, name, type);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private JTextField nameField(int playerNumber) {
        return find(view, "name" + playerNumber, JTextField.class);
    }

    @SuppressWarnings("unchecked")
    private JComboBox<Integer> playerCount() {
        return find(view, "playerCount", JComboBox.class);
    }

    @SuppressWarnings("unchecked")
    private JComboBox<String> cityBox() {
        return find(view, "city", JComboBox.class);
    }

    private JButton startButton() {
        return find(view, "start", JButton.class);
    }

    private JLabel errorLabel() {
        return find(view, "error", JLabel.class);
    }

    /** Waits for everything queued on the Swing thread. */
    private static void flush() {
        try {
            SwingUtilities.invokeAndWait(() -> { });
        } catch (InterruptedException | InvocationTargetException e) {
            throw new AssertionError(e);
        }
    }

    private void fill(String city, String... names) {
        playerCount().setSelectedItem(names.length);
        for (int i = 0; i < names.length; i++) {
            nameField(i + 1).setText(names[i]);
        }
        cityBox().setSelectedItem(city);
    }

    // ---- what the player sees at the start ----

    @Test
    void startsWithTwoNameFieldsAndNoCityChosen() {
        assertEquals(2, playerCount().getSelectedItem());
        assertTrue(nameField(1).isVisible());
        assertTrue(nameField(2).isVisible());
        assertFalse(nameField(3).isVisible());
        assertFalse(nameField(4).isVisible());
        assertNull(view.getSelectedCity());
        assertEquals("Start Game", startButton().getText());
        assertTrue(startButton().isEnabled());
        assertEquals(" ", errorLabel().getText());
    }

    @Test
    void offersTwoToFourPlayers() {
        JComboBox<Integer> box = playerCount();
        assertEquals(3, box.getItemCount());
        assertEquals(2, box.getItemAt(0));
        assertEquals(4, box.getItemAt(2));
    }

    @Test
    void offersTheCitiesFromTheViewModel() {
        JComboBox<String> box = cityBox();
        assertEquals(4, box.getItemCount());
        assertEquals("Toronto", box.getItemAt(0));
        assertEquals("New York", box.getItemAt(3));
    }

    @Test
    void thePlayerCountControlsHowManyNameFieldsShow() {
        playerCount().setSelectedItem(4);
        for (int i = 1; i <= 4; i++) {
            assertTrue(nameField(i).isVisible(), "field " + i);
        }
        playerCount().setSelectedItem(3);
        assertTrue(nameField(3).isVisible());
        assertFalse(nameField(4).isVisible());
        playerCount().setSelectedItem(2);
        assertFalse(nameField(3).isVisible());
    }

    // ---- starting a game ----

    @Test
    void startSendsTheNamesAndCityToTheController() {
        fill("Toronto", "Ann", "Bob", "Cat");
        startButton().doClick();

        assertEquals(1, started.size());
        assertEquals(List.of("Ann", "Bob", "Cat"), started.get(0).playerNames());
        assertEquals("Toronto", started.get(0).cityName());
    }

    @Test
    void namesTypedInHiddenFieldsAreIgnored() {
        playerCount().setSelectedItem(4);
        nameField(3).setText("Hidden");
        nameField(4).setText("Hidden too");
        playerCount().setSelectedItem(2);
        nameField(1).setText("Ann");
        nameField(2).setText("Bob");
        cityBox().setSelectedItem("London");
        startButton().doClick();

        assertEquals(List.of("Ann", "Bob"), started.get(0).playerNames());
    }

    @Test
    void noCityChosenSendsNullSoTheUseCaseCanComplain() {
        nameField(1).setText("Ann");
        nameField(2).setText("Bob");
        startButton().doClick();

        assertNull(started.get(0).cityName());
        assertEquals(List.of("Ann", "Bob"), started.get(0).playerNames());
    }

    @Test
    void emptyNamesAreSentAsTheyAreForTheUseCaseToJudge() {
        cityBox().setSelectedItem("Toronto");
        startButton().doClick();
        assertEquals(List.of("", ""), started.get(0).playerNames());
    }

    @Test
    void theButtonIsDisabledWhileTheGameStartsAndComesBack() {
        List<Runnable> queued = new ArrayList<>();
        Executor holding = queued::add;
        StartMenuView slow = new StartMenuView(viewModel, controller, holding);
        JButton button = find(slow, "start", JButton.class);

        button.doClick();
        assertFalse(button.isEnabled());
        assertEquals("Starting...", button.getText());
        assertTrue(started.isEmpty(), "nothing has run yet");

        queued.get(0).run();
        flush();
        assertEquals(1, started.size());
        assertTrue(button.isEnabled());
        assertEquals("Start Game", button.getText());
    }

    @Test
    void theButtonComesBackEvenIfStartingFails() {
        StartGameController failing = new StartGameController(data -> {
            throw new IllegalStateException("boom");
        });
        StartMenuView broken = new StartMenuView(viewModel, failing, command -> {
            try {
                command.run();
            } catch (IllegalStateException expected) {
                // the executor's thread would report this; the button must still come back
            }
        });
        JButton button = find(broken, "start", JButton.class);
        button.doClick();
        flush();
        assertTrue(button.isEnabled());
        assertEquals("Start Game", button.getText());
    }

    // ---- reacting to the view model ----

    @Test
    void anErrorFromThePresenterIsShown() {
        viewModel.setError("Please select a city.");
        flush();
        assertEquals("Please select a city.", errorLabel().getText());
    }

    @Test
    void theErrorIsClearedWhenTheViewModelClearsIt() {
        viewModel.setError("Please select a city.");
        flush();
        viewModel.setError("");
        flush();
        assertEquals(" ", errorLabel().getText());
    }

    @Test
    void updatesFromABackgroundThreadAreShown() throws InterruptedException {
        Thread worker = new Thread(() -> viewModel.setError("Player 2 needs a name."));
        worker.start();
        worker.join();
        flush();
        assertEquals("Player 2 needs a name.", errorLabel().getText());
    }

    @Test
    void aSuccessfulStartKeepsWhatWasTyped() {
        fill("Toronto", "Ann", "Bob");
        viewModel.setGameStarted(List.of("Ann", "Bob"), "Toronto", "w", "b");
        flush();
        assertEquals("Ann", nameField(1).getText());
        assertEquals("Toronto", view.getSelectedCity());
    }

    @Test
    void resettingTheMenuForANewGameClearsTheForm() {
        fill("London", "Ann", "Bob", "Cat", "Dan");
        viewModel.setGameStarted(List.of("Ann", "Bob", "Cat", "Dan"), "London", "w", "b");
        viewModel.reset();
        flush();

        assertEquals(2, playerCount().getSelectedItem());
        for (int i = 1; i <= 4; i++) {
            assertEquals("", nameField(i).getText(), "field " + i);
        }
        assertNull(view.getSelectedCity());
        assertFalse(nameField(3).isVisible());
        assertEquals(" ", errorLabel().getText());
    }

    // ---- construction ----

    @Test
    void constructorRejectsNulls() {
        assertThrows(NullPointerException.class, () -> new StartMenuView(null, controller, Runnable::run));
        assertThrows(NullPointerException.class, () -> new StartMenuView(viewModel, null, Runnable::run));
        assertThrows(NullPointerException.class, () -> new StartMenuView(viewModel, controller, null));
    }
}
