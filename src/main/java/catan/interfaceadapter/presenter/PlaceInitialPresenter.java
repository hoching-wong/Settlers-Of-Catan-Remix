package catan.interfaceadapter.presenter;

import catan.entity.ResourceType;
import catan.entity.SetupStep;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.BoardViewModel;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.placeinitial.PlaceInitialOutputBoundary;
import catan.usecase.placeinitial.PlaceInitialOutputData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Shows whose placement turn it is on the game screen, and redraws the board with the new
 * settlement or road. It also tells the board whether the next click places a settlement
 * (a corner) or a road (an edge).
 */
public class PlaceInitialPresenter implements PlaceInitialOutputBoundary {
    private final GameViewModel game;
    private final BoardViewModel board;

    /** For a screen without a board view. */
    public PlaceInitialPresenter(GameViewModel game) {
        this(game, new BoardViewModel());
    }

    public PlaceInitialPresenter(GameViewModel game, BoardViewModel board) {
        this.game = Objects.requireNonNull(game, "game");
        this.board = Objects.requireNonNull(board, "board");
    }

    @Override
    public void presentPlacement(PlaceInitialOutputData out) {
        String instruction;
        if (out.setupComplete()) {
            instruction = Text.ROLL_DICE;
        } else if (out.nextStep() == SetupStep.ROAD) {
            instruction = Text.PLACE_ROAD;
        } else {
            instruction = Text.PLACE_SETTLEMENT;
        }
        game.setTurn(out.nextPlayerName(), instruction);
        game.setMessage(message(out));

        BoardState state = board.getState();
        BoardStateMapper.applySnapshot(state, out.board());
        state.setSetupTarget(setupTarget(out));
        board.firePropertyChanged();
    }

    /** What the next click on the board places, or null once regular play has begun. */
    private static String setupTarget(PlaceInitialOutputData out) {
        if (out.setupComplete()) {
            return null;
        }
        return out.nextStep() == SetupStep.ROAD ? BoardState.EDGE : BoardState.VERTEX;
    }

    @Override
    public void presentError(String message) {
        game.setError(message);
    }

    private static String message(PlaceInitialOutputData out) {
        if (!out.resourcesGained().isEmpty()) {
            return out.playerName() + " received " + gains(out.resourcesGained()) + ".";
        }
        if (out.setupComplete()) {
            return "Setup complete.";
        }
        return "";
    }

    private static String gains(Map<ResourceType, Integer> gained) {
        List<String> parts = new ArrayList<>();
        for (Map.Entry<ResourceType, Integer> e : gained.entrySet()) {
            parts.add(e.getValue() + " " + Text.title(e.getKey()));
        }
        return String.join(", ", parts);
    }
}
