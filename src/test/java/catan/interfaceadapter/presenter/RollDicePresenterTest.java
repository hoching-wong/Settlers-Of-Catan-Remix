package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.rolldice.RollDiceOutputData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Checks what the presenter puts into the view model. How the view model handles the turn,
 * message and error is tested in GameViewModelTest.
 */
class RollDicePresenterTest {
    private final GameViewModel game = new GameViewModel();
    private final RollDicePresenter presenter = new RollDicePresenter(game);

    private static RollDiceOutputData rolled(String player, int dieOne, int dieTwo,
                                             Map<String, Map<String, Integer>> gains) {
        return new RollDiceOutputData(player, dieOne, dieTwo, dieOne + dieTwo, gains);
    }

    // ---- a roll ----

    @Test
    void theRollIsShownAndThePlayerIsToldWhatToDoNext() {
        Map<String, Map<String, Integer>> gains = new LinkedHashMap<>();
        gains.put("Bob", Map.of("LUMBER", 2));
        gains.put("Ann", Map.of("GRAIN", 1));

        presenter.presentRolled(rolled("Ann", 3, 5, gains));

        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Build or end your turn", game.getInstruction());
        assertEquals("Ann rolled 3 + 5 = 8. Bob +2 Lumber, Ann +1 Grain.", game.getMessage());
    }

    @Test
    void aPlayerWhoReceivesSeveralResourcesHasThemAllListed() {
        Map<String, Integer> bobsGains = new LinkedHashMap<>();
        bobsGains.put("LUMBER", 2);
        bobsGains.put("ORE", 1);
        Map<String, Map<String, Integer>> gains = new LinkedHashMap<>();
        gains.put("Bob", bobsGains);
        gains.put("Ann", Map.of("GRAIN", 1));

        presenter.presentRolled(rolled("Ann", 4, 4, gains));

        assertEquals("Ann rolled 4 + 4 = 8. Bob +2 Lumber +1 Ore, Ann +1 Grain.", game.getMessage());
    }

    @Test
    void aSevenSaysNothingIsProduced() {
        presenter.presentRolled(rolled("Ann", 6, 1, Map.of()));

        assertEquals("Ann rolled 7. No resources are produced.", game.getMessage());
        assertEquals("Build or end your turn", game.getInstruction());
    }

    @Test
    void whenNobodyReceivesAnythingItSaysSo() {
        presenter.presentRolled(rolled("Ann", 4, 2, Map.of()));

        assertEquals("Ann rolled 4 + 2 = 6. No one received resources.", game.getMessage());
    }

    @Test
    void successClearsTheEarlierError() {
        presenter.presentError("You have already rolled this turn.");
        presenter.presentRolled(rolled("Ann", 4, 2, Map.of()));

        assertEquals("", game.getErrorMessage());
    }

    @Test
    void aRollTellsTheViewTheTurnAndTheMessageChanged() {
        List<String> events = new ArrayList<>();
        game.addPropertyChangeListener(event -> events.add(event.getPropertyName()));

        presenter.presentRolled(rolled("Ann", 4, 2, Map.of()));

        assertTrue(events.contains(GameViewModel.TURN));
        assertTrue(events.contains(GameViewModel.MESSAGE));
    }

    // ---- a refusal ----

    @Test
    void aRefusalShowsTheReasonAndChangesNothingElse() {
        game.setTurn("Ann", "Roll the dice");
        game.setMessage("Bob ended their turn.");

        presenter.presentError("You have already rolled this turn.");

        assertEquals("You have already rolled this turn.", game.getErrorMessage());
        assertEquals("Ann", game.getCurrentPlayerName());
        assertEquals("Roll the dice", game.getInstruction());
        assertEquals("Bob ended their turn.", game.getMessage());
    }

    // ---- construction ----

    @Test
    void rejectsANullViewModel() {
        assertThrows(NullPointerException.class, () -> new RollDicePresenter(null));
    }
}
