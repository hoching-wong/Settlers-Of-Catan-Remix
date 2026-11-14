package catan.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Works out who receives what for a dice roll. Stateless. */
public class ProductionCalculator {

    /**
     * The resources each player receives for the dice total. A settlement produces 1 of a
     * matching tile's resource and a city produces 2. When the weather has a bonus, the
     * production of that one resource is multiplied, and only from tiles that were rolled.
     * A total of 7 produces nothing, and players who receive nothing are left out of the result.
     *
     * <p>This only calculates. It does not change any player's hand, so the caller adds the
     * result to each player's resources.
     */
    public static Map<Player, Map<ResourceType, Integer>> distribute(
            Board board, int diceTotal, WeatherData weather) {
        Objects.requireNonNull(board, "board");
        Objects.requireNonNull(weather, "weather");

        Map<Player, Map<ResourceType, Integer>> result = new LinkedHashMap<>();
        if (diceTotal == 7) {
            return Collections.unmodifiableMap(result);
        }
        for (Tile tile : board.getMatchingTiles(diceTotal)) {
            ResourceType resource = tile.getResourceType();
            int multiplier = weather.hasBonus() && resource == weather.getBonusResource()
                    ? weather.getMultiplier()
                    : 1;
            for (Vertex vertex : tile.getVertices()) {
                Building building = vertex.getBuilding();
                if (building == null) {
                    continue;
                }
                int amount = (building instanceof City ? 2 : 1) * multiplier;
                result.computeIfAbsent(building.getOwner(), p -> new EnumMap<>(ResourceType.class))
                        .merge(resource, amount, Integer::sum);
            }
        }
        result.replaceAll((player, gains) -> Collections.unmodifiableMap(gains));
        return Collections.unmodifiableMap(result);
    }
}
