package usecase.build;

import entity.BuildCost;
import entity.Game;
import entity.Player;
import entity.ResourceType;
import usecase.boardstate.BoardSnapshot;
import usecase.build.placement.PlacementStrategies;
import usecase.build.placement.PlacementStrategy;
import usecase.build.rules.BuildRequest;
import usecase.build.rules.BuildRule;
import usecase.build.rules.BuildRules;
import usecase.port.GameRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Use case 1. Checks the build with a chain of rules (turn, dice, cost, location), then pays,
 * places the structure, updates Longest Road (use case 6) and checks for a winner (use case 7).
 */
public class BuildInteractor implements BuildInputBoundary {
    private final GameRepository repository;
    private final BuildOutputBoundary presenter;
    private final PlacementStrategies placements;
    private final BuildRule rules;

    /** The normal road, settlement and city rules. */
    public BuildInteractor(GameRepository repository, BuildOutputBoundary presenter) {
        this(repository, presenter, PlacementStrategies.standard(), BuildRules.forPlacing());
    }

    public BuildInteractor(GameRepository repository, BuildOutputBoundary presenter,
                           PlacementStrategies placements, BuildRule rules) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        this.placements = Objects.requireNonNull(placements, "placements");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    @Override
    public void execute(BuildInputData inputData) {
        Game game = repository.getGame();
        if (game == null) {
            presenter.prepareFailView("No game is in progress.");
            return;
        }
        PlacementStrategy placement = placements.get(inputData.structure());
        Optional<String> error = rules.check(new BuildRequest(game, placement, inputData.locationId()));
        if (error.isPresent()) {
            presenter.prepareFailView(error.get());
            return;
        }

        Player player = game.getCurrentPlayer();
        player.getResources().spendResources(BuildCost.of(inputData.structure()));
        placement.place(game.getBoard(), player, inputData.locationId());

        Player holder = game.updateLongestRoadHolder();
        boolean gameOver = game.checkVictory();
        repository.saveGame(game);

        presenter.prepareSuccessView(new BuildOutputData(
                inputData.structure().name(),
                inputData.locationId(),
                player.getName(),
                player.getVictoryPoints(),
                resourcesOf(player),
                holder == null ? null : holder.getName(),
                gameOver,
                gameOver ? game.getWinner().getName() : null,
                BoardSnapshot.of(game.getBoard(), game.getPlayers())));
    }

    private static Map<String, Integer> resourcesOf(Player player) {
        Map<String, Integer> result = new LinkedHashMap<>();
        for (ResourceType type : ResourceType.values()) {
            result.put(type.name(), player.getResources().getResourceCount(type));
        }
        return result;
    }
}
