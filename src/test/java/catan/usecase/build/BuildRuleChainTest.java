package catan.usecase.build;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Game;
import catan.entity.StructureType;
import catan.usecase.TestGames;
import catan.usecase.build.placement.PlacementStrategies;
import catan.usecase.build.rules.BuildRequest;
import catan.usecase.build.rules.BuildRule;
import catan.usecase.build.rules.BuildRules;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class BuildRuleChainTest {
    private final PlacementStrategies placements = PlacementStrategies.standard();

    private BuildRequest road(Game game) {
        return new BuildRequest(game, placements.get(StructureType.ROAD), BuildRequest.NO_LOCATION);
    }

    @Test
    void firstFailingRuleWins() {
        Game game = TestGames.playingGame(2, "Ann", "Bob");
        // not rolled AND no resources: the dice message comes first
        assertEquals(Optional.of("Roll the dice before building."), BuildRules.beforeChoosingLocation().check(road(game)));
        game.markRolled();
        assertTrue(BuildRules.beforeChoosingLocation().check(road(game)).orElseThrow().startsWith("Not enough resources"));
        TestGames.give(game.getCurrentPlayer(), 1);
        assertEquals(Optional.empty(), BuildRules.beforeChoosingLocation().check(road(game)));
    }

    @Test
    void setupPhaseIsRejected() {
        Game game = TestGames.setupGame(2, "Ann", "Bob");
        assertTrue(BuildRules.forPlacing().check(road(game)).orElseThrow().contains("starting settlements"));
    }

    @Test
    void costMessageListsTheResources() {
        Game game = TestGames.playingGame(2, "Ann", "Bob");
        game.markRolled();
        BuildRequest city = new BuildRequest(game, placements.get(StructureType.CITY), 0);
        assertEquals(Optional.of("Not enough resources. A city costs 2 grain, 3 ore."),
                BuildRules.forPlacing().check(city));
    }

    @Test
    void chainStopsAtTheFirstFailure() {
        List<String> ran = new ArrayList<>();
        class Named extends BuildRule {
            final String name;
            final boolean pass;

            Named(String name, boolean pass) {
                this.name = name;
                this.pass = pass;
            }

            @Override
            protected Optional<String> checkThis(BuildRequest request) {
                ran.add(name);
                return pass ? Optional.empty() : Optional.of(name);
            }
        }
        BuildRule head = new Named("a", true);
        head.linkWith(new Named("b", false)).linkWith(new Named("c", true));
        assertEquals(Optional.of("b"), head.check(null));
        assertEquals(List.of("a", "b"), ran);
    }
}
