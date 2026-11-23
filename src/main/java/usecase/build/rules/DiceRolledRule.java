package usecase.build.rules;

import java.util.Optional;

/** A player builds after rolling the dice and collecting resources. */
public class DiceRolledRule extends BuildRule {
    @Override
    protected Optional<String> checkThis(BuildRequest request) {
        if (!request.game().hasRolled()) {
            return Optional.of("Roll the dice before building.");
        }
        return Optional.empty();
    }
}
