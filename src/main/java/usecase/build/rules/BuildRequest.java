package usecase.build.rules;

import entity.Game;
import entity.Player;
import entity.StructureType;
import usecase.build.placement.PlacementStrategy;

/**
 * What a rule needs to decide whether a build is allowed.
 *
 * @param locationId the vertex or edge id, or {@link #NO_LOCATION} when only the structure has
 *                   been chosen (no location clicked yet)
 */
public record BuildRequest(Game game, PlacementStrategy placement, int locationId) {
    public static final int NO_LOCATION = -1;

    public Player player() {
        return game.getCurrentPlayer();
    }

    public StructureType structureType() {
        return placement.structureType();
    }
}
