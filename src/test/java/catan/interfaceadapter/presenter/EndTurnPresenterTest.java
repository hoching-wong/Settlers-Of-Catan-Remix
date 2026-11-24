package catan.interfaceadapter.presenter;

import static org.junit.jupiter.api.Assertions.*;

import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.endturn.EndTurnOutputData;
import org.junit.jupiter.api.Test;

class EndTurnPresenterTest {
    private final GameViewModel game = new GameViewModel();
    private final EndTurnPresenter presenter = new EndTurnPresenter(game);

    @Test
    void theNextPlayerIsToldToRoll() {
        presenter.presentTurnEnded(new EndTurnOutputData("Ann", "Bob"));
        assertEquals("Bob", game.getCurrentPlayerName());
        assertEquals("Roll the dice", game.getInstruction());
        assertEquals("Ann ended their turn.", game.getMessage());
    }

    @Test
    void aRefusalShowsTheReasonAndKeepsTheTurn() {
        game.setTurn("Ann", "Roll the dice");
        presenter.presentError("Roll the dice before ending your turn.");
        assertEquals("Roll the dice before ending your turn.", game.getErrorMessage());
        assertEquals("Ann", game.getCurrentPlayerName());
    }

    @Test
    void successClearsTheEarlierError() {
        presenter.presentError("Roll the dice before ending your turn.");
        presenter.presentTurnEnded(new EndTurnOutputData("Ann", "Bob"));
        assertEquals("", game.getErrorMessage());
    }

    @Test
    void rejectsANullViewModel() {
        assertThrows(NullPointerException.class, () -> new EndTurnPresenter(null));
    }
}
