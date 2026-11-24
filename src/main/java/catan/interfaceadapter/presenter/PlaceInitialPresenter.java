package catan.interfaceadapter.presenter;

import catan.entity.ResourceType;
import catan.entity.SetupStep;
import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.placeinitial.PlaceInitialOutputBoundary;
import catan.usecase.placeinitial.PlaceInitialOutputData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlaceInitialPresenter implements PlaceInitialOutputBoundary {
    private final GameViewModel game;

    public PlaceInitialPresenter(GameViewModel game) {
        this.game = Objects.requireNonNull(game, "game");
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
