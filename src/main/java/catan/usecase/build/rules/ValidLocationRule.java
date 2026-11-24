package catan.usecase.build.rules;

import java.util.Optional;

/** The chosen vertex or edge must follow the placement rules of the structure. */
public class ValidLocationRule extends BuildRule {
    @Override
    protected Optional<String> checkThis(BuildRequest request) {
        boolean valid = request.placement().isValidLocation(
                request.game().getBoard(), request.player(), request.locationId());
        return valid ? Optional.empty() : Optional.of(request.placement().invalidLocationMessage());
    }
}
