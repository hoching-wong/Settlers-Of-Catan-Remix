package catan.interfaceadapter.controller;

import static org.junit.jupiter.api.Assertions.*;

import catan.usecase.endturn.EndTurnInputBoundary;
import catan.usecase.endturn.EndTurnInputData;
import catan.usecase.placeinitial.PlaceInitialInputBoundary;
import catan.usecase.placeinitial.PlaceInitialInputData;
import catan.usecase.restartgame.RestartGameInputBoundary;
import catan.usecase.restartgame.RestartGameInputData;
import catan.usecase.restartgame.RestartGameInputData.Choice;
import catan.usecase.startgame.StartGameInputBoundary;
import catan.usecase.startgame.StartGameInputData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Each controller turns a click into the input data for its use case. */
class ControllersTest {

    @Test
    void startGameControllerPassesNamesAndCity() {
        List<StartGameInputData> received = new ArrayList<>();
        StartGameInputBoundary boundary = received::add;

        new StartGameController(boundary).startGame(List.of("Ann", "Bob"), "Toronto");

        assertEquals(1, received.size());
        assertEquals(List.of("Ann", "Bob"), received.get(0).playerNames());
        assertEquals("Toronto", received.get(0).cityName());
    }

    @Test
    void startGameControllerPassesAMissingCityOn() {
        List<StartGameInputData> received = new ArrayList<>();
        new StartGameController(received::add).startGame(List.of("Ann", "Bob"), null);
        assertNull(received.get(0).cityName());
    }

    @Test
    void placeInitialControllerPassesTheClickedId() {
        List<PlaceInitialInputData> received = new ArrayList<>();
        PlaceInitialInputBoundary boundary = received::add;

        PlaceInitialController controller = new PlaceInitialController(boundary);
        controller.place(17);
        controller.place(0);

        assertEquals(List.of(new PlaceInitialInputData(17), new PlaceInitialInputData(0)), received);
    }

    @Test
    void endTurnControllerCallsTheUseCase() {
        List<EndTurnInputData> received = new ArrayList<>();
        EndTurnInputBoundary boundary = received::add;

        new EndTurnController(boundary).endTurn();

        assertEquals(1, received.size());
    }

    @Test
    void restartControllerSendsTheButtonPressed() {
        List<RestartGameInputData> received = new ArrayList<>();
        RestartGameInputBoundary boundary = received::add;
        RestartGameController controller = new RestartGameController(boundary);

        controller.newGame();
        controller.exit();

        assertEquals(List.of(new RestartGameInputData(Choice.NEW_GAME), new RestartGameInputData(Choice.EXIT)),
                received);
    }

    @Test
    void controllersRejectANullUseCase() {
        assertThrows(NullPointerException.class, () -> new StartGameController(null));
        assertThrows(NullPointerException.class, () -> new PlaceInitialController(null));
        assertThrows(NullPointerException.class, () -> new EndTurnController(null));
        assertThrows(NullPointerException.class, () -> new RestartGameController(null));
    }
}
