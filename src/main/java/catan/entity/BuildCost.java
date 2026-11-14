package catan.entity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

public class BuildCost {

    public static Map<ResourceType, Integer> of(StructureType type) {
        Map<ResourceType, Integer> cost = new EnumMap<>(ResourceType.class);
        switch (type) {
            case ROAD -> {
                cost.put(ResourceType.LUMBER, 1);
                cost.put(ResourceType.BRICK, 1);
            }
            case SETTLEMENT -> {
                cost.put(ResourceType.LUMBER, 1);
                cost.put(ResourceType.BRICK, 1);
                cost.put(ResourceType.WOOL, 1);
                cost.put(ResourceType.GRAIN, 1);
            }
            case CITY -> {
                cost.put(ResourceType.GRAIN, 2);
                cost.put(ResourceType.ORE, 3);
            }
        }
        return Collections.unmodifiableMap(cost);
    }
}
