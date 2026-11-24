package catan.usecase.selectstructure;

import catan.entity.Game;
import catan.usecase.build.placement.PlacementStrategies;
import catan.usecase.build.placement.PlacementStrategy;
import catan.usecase.build.rules.BuildRequest;
import catan.usecase.build.rules.BuildRule;
import catan.usecase.build.rules.BuildRules;
import catan.usecase.port.GameRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Checks that the player may build the chosen structure at all (same rule chain as the build,
 * minus the location), then lists every place it could go. Nothing on the board changes.
 */
public class SelectStructureInteractor implements SelectStructureInputBoundary {
    private final GameRepository repository;
    private final SelectStructureOutputBoundary presenter;
    private final PlacementStrategies placements;
    private final BuildRule rules;

    public SelectStructureInteractor(GameRepository repository, SelectStructureOutputBoundary presenter) {
        this(repository, presenter, PlacementStrategies.standard(), BuildRules.beforeChoosingLocation());
    }

    public SelectStructureInteractor(GameRepository repository, SelectStructureOutputBoundary presenter,
                                     PlacementStrategies placements, BuildRule rules) {
        this.repository = Objects.requireNonNull(repository, "repository");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        this.placements = Objects.requireNonNull(placements, "placements");
        this.rules = Objects.requireNonNull(rules, "rules");
    }

    @Override
    public void execute(SelectStructureInputData inputData) {
        Game game = repository.get();
        if (game == null) {
            presenter.prepareFailView("No game is in progress.");
            return;
        }
        PlacementStrategy placement = placements.get(inputData.structure());
        Optional<String> error = rules.check(new BuildRequest(game, placement, BuildRequest.NO_LOCATION));
        if (error.isPresent()) {
            presenter.prepareFailView(error.get());
            return;
        }
        List<Integer> valid = placement.validLocations(game.getBoard(), game.getCurrentPlayer());
        if (valid.isEmpty()) {
            presenter.prepareFailView("There is nowhere you can build a "
                    + inputData.structure().name().toLowerCase(java.util.Locale.ROOT) + " right now.");
            return;
        }
        presenter.prepareSuccessView(new SelectStructureOutputData(
                inputData.structure().name(), placement.locationKind().name(), valid,
                game.getCurrentPlayer().getName()));
    }
}
