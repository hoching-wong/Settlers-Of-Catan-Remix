package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.ResourceType;
import catan.entity.SetupStep;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.placeinitial.PlaceInitialOutputData;
import java.util.EnumMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlaceInitialPresenterTest {
    private final GameViewModel game = new GameViewModel();
    private final PlaceInitialPresenter presenter = new PlaceInitialPresenter(game);

    @Test
    void afterASettlementTheSamePlayerPlacesARoad() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD));
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a road next to your new settlement", game.getInstruction());
        assertEquals("", game.getMessage());
    }

    @Test
    void afterARoadTheNextPlayerPlacesASettlement() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 8, Map.of(),
                false, "Bob", SetupStep.SETTLEMENT));
        assertEquals("Bob", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
    }

    @Test
    void startingResourcesAreAnnounced() {
        Map<ResourceType, Integer> gained = new EnumMap<>(ResourceType.class);
        gained.put(ResourceType.GRAIN, 2);
        gained.put(ResourceType.LUMBER, 1);
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Bob", 20, gained,
                false, "Bob", SetupStep.ROAD));
        assertEquals("Bob received 1 Lumber, 2 Grain.", game.getMessage());
    }

    @Test
    void whenSetupEndsPlayerOneRolls() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.ROAD, "Ann", 30, Map.of(),
                true, "Ann", null));
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Roll the dice", game.getInstruction());
        assertEquals("Setup complete.", game.getMessage());
    }

    @Test
    void anAnnouncementOfStartingResourcesBeatsTheSetupCompleteNote() {
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 9,
                Map.of(ResourceType.ORE, 1), false, "Ann", SetupStep.ROAD));
        assertEquals("Ann received 1 Ore.", game.getMessage());
    }

    @Test
    void anErrorKeepsTheTurnAndShowsTheReason() {
        game.setTurn("Ann", "Place a settlement");
        presenter.presentError("That spot is already taken.");
        assertEquals("That spot is already taken.", game.getErrorMessage());
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Place a settlement", game.getInstruction());
    }

    @Test
    void aSuccessfulPlacementClearsTheError() {
        presenter.presentError("That spot is already taken.");
        presenter.presentPlacement(new PlaceInitialOutputData(SetupStep.SETTLEMENT, "Ann", 5, Map.of(),
                false, "Ann", SetupStep.ROAD));
        assertEquals("", game.getErrorMessage());
    }

    @Test
    void rejectsANullViewModel() {
        assertThrows(NullPointerException.class, () -> new PlaceInitialPresenter(null));
    }
}
