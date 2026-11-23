package usecase.build.rules;

import entity.BuildCost;
import entity.ResourceType;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/** The player must have the resources the structure costs. */
public class AffordabilityRule extends BuildRule {
    @Override
    protected Optional<String> checkThis(BuildRequest request) {
        Map<ResourceType, Integer> cost = BuildCost.of(request.structureType());
        if (request.player().getResources().canAfford(cost)) {
            return Optional.empty();
        }
        return Optional.of("Not enough resources. A " + name(request) + " costs " + describe(cost) + ".");
    }

    private static String name(BuildRequest request) {
        return request.structureType().name().toLowerCase(Locale.ROOT);
    }

    /** For example "1 lumber, 1 brick". */
    static String describe(Map<ResourceType, Integer> cost) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(e.getValue()).append(' ').append(e.getKey().name().toLowerCase(Locale.ROOT));
        }
        return sb.toString();
    }
}
