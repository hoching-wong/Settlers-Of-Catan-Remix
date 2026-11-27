package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.GameViewModel;
import catan.usecase.rolldice.RollDiceOutputBoundary;
import catan.usecase.rolldice.RollDiceOutputData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Shows the roll and what each player received, and tells the player what to do next. */
public class RollDicePresenter implements RollDiceOutputBoundary {
    private static final int SEVEN = 7;

    private final GameViewModel game;

    public RollDicePresenter(GameViewModel game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    @Override
    public void presentRolled(RollDiceOutputData out) {
        game.setTurn(out.playerName(), Text.BUILD_OR_END_TURN);
        game.setMessage(describe(out));
    }

    @Override
    public void presentError(String message) {
        game.setError(message);
    }

    private static String describe(RollDiceOutputData out) {
        if (out.total() == SEVEN) {
            return out.playerName() + " rolled 7. No resources are produced.";
        }
        String rolled = out.playerName() + " rolled " + out.dieOne() + " + " + out.dieTwo() + " = " + out.total() + ".";
        if (out.gains().isEmpty()) {
            return rolled + " No one received resources.";
        }
        List<String> players = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> player : out.gains().entrySet()) {
            List<String> resources = new ArrayList<>();
            for (Map.Entry<String, Integer> resource : player.getValue().entrySet()) {
                resources.add("+" + resource.getValue() + " " + Text.title(resource.getKey()));
            }
            players.add(player.getKey() + " " + String.join(" ", resources));
        }
        return rolled + " " + String.join(", ", players) + ".";
    }
}
