package catan.usecase.rolldice;

import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ProductionCalculator;
import catan.entity.ResourceType;
import catan.usecase.port.GameRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Use case 2. The current player rolls the dice, and every player with a settlement or city
 * next to a tile with that number receives its resources.
 */
public class RollDiceInteractor implements RollDiceInputBoundary {
    private final GameRepository gameRepository;
    private final RollDiceOutputBoundary outputBoundary;

    public RollDiceInteractor(GameRepository gameRepository, RollDiceOutputBoundary outputBoundary) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void rollDice(RollDiceInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

        Game game = gameRepository.get();
        if (game == null) {
            outputBoundary.presentError("No game is in progress.");
            return;
        }
        if (game.getPhase() == GamePhase.GAME_OVER) {
            outputBoundary.presentError("The game is over.");
            return;
        }
        if (game.getPhase() == GamePhase.SETUP) {
            outputBoundary.presentError("Finish placing the starting settlements and roads first.");
            return;
        }
        if (!game.canRoll()) {
            outputBoundary.presentError("You have already rolled this turn.");
            return;
        }

        Dice dice = game.getDice();
        dice.roll();
        Map<Player, Map<ResourceType, Integer>> produced =
                ProductionCalculator.distribute(game.getBoard(), dice.getTotal(), game.getWeather());
        Map<String, Map<String, Integer>> gains = new LinkedHashMap<>();
        produced.forEach((player, resources) -> {
            Map<String, Integer> byName = new LinkedHashMap<>();
            resources.forEach((type, amount) -> {
                player.getResources().addResource(type, amount);
                byName.put(type.name(), amount);
            });
            gains.put(player.getName(), byName);
        });
        game.markRolled();
        gameRepository.save(game);

        outputBoundary.presentRolled(new RollDiceOutputData(
                game.getCurrentPlayer().getName(), dice.getDie1(), dice.getDie2(), dice.getTotal(), gains));
    }
}
