package catan.usecase.placeinitial;

import catan.entity.ResourceType;
import catan.entity.SetupStep;
import java.util.Map;

/**
 * What changed after a successful placement.
 *
 * @param placed SETTLEMENT or ROAD: what was just placed
 * @param playerName the player who placed it
 * @param locationId the vertex or edge id
 * @param resourcesGained the starting resources the player received (only after a second
 *     settlement; otherwise empty)
 * @param setupComplete true once the last road is placed and regular play has begun
 * @param nextPlayerName the player whose turn it is now (Player 1 once setup is complete)
 * @param nextStep what is expected next, or null once setup is complete
 */
public record PlaceInitialOutputData(
        SetupStep placed,
        String playerName,
        int locationId,
        Map<ResourceType, Integer> resourcesGained,
        boolean setupComplete,
        String nextPlayerName,
        SetupStep nextStep) {
}
