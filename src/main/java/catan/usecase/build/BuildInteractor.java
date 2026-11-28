package catan.usecase.build;

import catan.entity.BuildCost;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.usecase.boardstate.BoardSnapshot;
import catan.usecase.build.placement.PlacementStrategies;
import catan.usecase.build.placement.PlacementStrategy;
import catan.usecase.build.rules.BuildRequest;
import catan.usecase.build.rules.BuildRule;
import catan.usecase.build.rules.BuildRules;
import catan.usecase.longestroad.LongestRoadInputBoundary;
import catan.usecase.longestroad.LongestRoadInputData;
import catan.usecase.longestroad.LongestRoadOutputData;
import catan.usecase.port.GameRepository;
import catan.usecase.recordwin.RecordWinInputBoundary;
import catan.usecase.recordwin.RecordWinInputData;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Use case 1. Checks the build with a chain of rules (turn, dice, cost, location), then pays,
 * places the structure, updates Longest Road (use case 6), checks for a winner (use case 7) and
 * records the winner's win (use case 9).
 */
public class BuildInteractor implements BuildInputBoundary {
    private final GameRepository repository;
    private final BuildOutputBoundary presenter;
    private final PlacementStrategies placements;
    private final BuildRule rules;
    private final LongestRoadInputBoundary longestRoadInteractor;
    private final RecordWinInputBoundary recordWin;

    /** The normal road, settlement and city rules. */
    public BuildInteractor(GameRepository repository, BuildOutputBoundary presenter,
                           LongestRoadInputBoundary longestRoadInteractor,
                           RecordWinInputBoundary recordWin) {
        this(repository, presenter, PlacementStrategies.standard(),
                BuildRules.forPlacing(), longestRoadInteractor, recordWin);
    }

    public BuildInteractor(GameRepository repository, BuildOutputBoundary presenter,
                           PlacementStrategies placements, BuildRule rules,
                           LongestRoadInputBoundary longestRoadInteractor,
                           RecordWinInputBoundary recordWin) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        this.placements = Objects.requireNonNull(placements, "placements");
        this.rules = Objects.requireNonNull(rules, "rules");
        this.longestRoadInteractor = Objects.requireNonNull(
                longestRoadInteractor, "longestRoadInteractor");
        this.recordWin = Objects.requireNonNull(recordWin, "recordWin");
    }

    @Override
    public void execute(BuildInputData inputData) {
        Game game = repository.get();
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

        LongestRoadOutputData longestRoadOutputData =
                longestRoadInteractor.execute(new LongestRoadInputData(game));

        boolean gameOver = game.checkVictory();
        repository.save(game);
        if (gameOver) {
            recordWin.recordWin(new RecordWinInputData(game.getWinner().getName()));
        }

        presenter.prepareSuccessView(new BuildOutputData(
                inputData.structure().name(),
                inputData.locationId(),
                player.getName(),
                player.getVictoryPoints(),
                resourcesOf(player),
                longestRoadOutputData.holderName(),
                longestRoadOutputData.roadLength(),
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
