package usecase.build.rules;

import entity.GamePhase;
import java.util.Optional;

/** Building with resources is only allowed during regular play. */
public class GameInProgressRule extends BuildRule {
    @Override
    protected Optional<String> checkThis(BuildRequest request) {
        GamePhase phase = request.game().getPhase();
        if (phase == GamePhase.SETUP) {
            return Optional.of("Finish placing the starting settlements and roads first.");
        }
        if (phase == GamePhase.GAME_OVER) {
            return Optional.of("The game is over.");
        }
        return Optional.empty();
    }
}
