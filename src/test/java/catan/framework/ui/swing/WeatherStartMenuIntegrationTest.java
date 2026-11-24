package catan.framework.ui.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import catan.entity.CityLocation;
import catan.interfaceadapter.controller.StartGameController;
import catan.interfaceadapter.viewmodel.StartMenuViewModel;
import catan.usecase.startgame.StartGameInputData;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;

/**
 * Tests integration between the start menu and weather selection panel.
 */
class WeatherStartMenuIntegrationTest {

    /**
     * Tests that the start menu uses WeatherSelectionPanel
     * and passes the selected city to the controller.
     *
     * @throws Exception if a Swing operation fails
     */
    @Test
    void weatherPanelIsIntegratedIntoStartMenu() throws Exception {
        StartMenuViewModel viewModel = new StartMenuViewModel(
                CityLocation.defaults().stream()
                        .map(CityLocation::getName)
                        .toList());

        List<StartGameInputData> requests = new ArrayList<>();

        StartGameController controller =
                new StartGameController(requests::add);

        StartMenuView startMenu =
                new StartMenuView(viewModel, controller, Runnable::run);

        WeatherSelectionPanel weatherPanel =
                findComponent(startMenu, WeatherSelectionPanel.class);

        assertNotNull(weatherPanel);

        SwingUtilities.invokeAndWait(() -> {
            JComboBox<?> dropdown =
                    findNamedComponent(
                            weatherPanel, "city", JComboBox.class);

            assertNotNull(dropdown);
            assertEquals(6, dropdown.getItemCount());
            assertNull(weatherPanel.getSelectedCity());

            dropdown.setSelectedItem("Paris");

            JTextField player1 =
                    findNamedComponent(
                            startMenu, "name1", JTextField.class);

            JTextField player2 =
                    findNamedComponent(
                            startMenu, "name2", JTextField.class);

            assertNotNull(player1);
            assertNotNull(player2);

            player1.setText("Alice");
            player2.setText("Bob");

            JButton startButton =
                    findNamedComponent(
                            startMenu, "start", JButton.class);

            assertNotNull(startButton);
            startButton.doClick();
        });

        assertEquals(1, requests.size());
        assertEquals("Paris", requests.get(0).cityName());
        assertEquals(
                List.of("Alice", "Bob"),
                requests.get(0).playerNames());
    }

    /**
     * Tests that restarting clears the embedded weather selection.
     *
     * @throws Exception if a Swing operation fails
     */
    @Test
    void restartClearsWeatherSelection() throws Exception {
        StartMenuViewModel viewModel = new StartMenuViewModel(
                CityLocation.defaults().stream()
                        .map(CityLocation::getName)
                        .toList());

        StartGameController controller =
                new StartGameController(inputData -> { });

        StartMenuView startMenu =
                new StartMenuView(viewModel, controller, Runnable::run);

        WeatherSelectionPanel weatherPanel =
                findComponent(startMenu, WeatherSelectionPanel.class);

        assertNotNull(weatherPanel);

        SwingUtilities.invokeAndWait(() -> {
            JComboBox<?> dropdown =
                    findNamedComponent(
                            weatherPanel, "city", JComboBox.class);

            assertNotNull(dropdown);
            dropdown.setSelectedItem("Tokyo");

            assertEquals("Tokyo", weatherPanel.getSelectedCity());

            viewModel.setGameStarted(
                    List.of("Alice", "Bob"),
                    "Tokyo",
                    "Weather in Tokyo: Sunny",
                    "Bonus: Lumber x2");

            viewModel.reset();

            assertNull(weatherPanel.getSelectedCity());
        });
    }

    /**
     * Recursively finds a component of the requested type.
     *
     * @param root the root container
     * @param type the component type
     * @param <T> the component type parameter
     * @return the matching component, or null
     */
    private static <T extends Component> T findComponent(
            Container root, Class<T> type) {

        for (Component component : root.getComponents()) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }

            if (component instanceof Container container) {
                T found = findComponent(container, type);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    /**
     * Recursively finds a component with the requested name.
     *
     * @param root the root container
     * @param name the component name
     * @param type the component type
     * @param <T> the component type parameter
     * @return the matching component, or null
     */
    private static <T extends Component> T findNamedComponent(
            Container root, String name, Class<T> type) {

        for (Component component : root.getComponents()) {
            if (name.equals(component.getName())
                    && type.isInstance(component)) {
                return type.cast(component);
            }

            if (component instanceof Container container) {
                T found = findNamedComponent(container, name, type);

                if (found != null) {
                    return found;
                }
            }
        }

        return null;
    }
}

