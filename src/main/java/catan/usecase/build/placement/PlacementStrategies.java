package catan.usecase.build.placement;

import catan.entity.StructureType;
import java.util.EnumMap;
import java.util.Map;

/** Looks up the {@link PlacementStrategy} for each structure type. */
public class PlacementStrategies {
    private final Map<StructureType, PlacementStrategy> byType = new EnumMap<>(StructureType.class);

    public PlacementStrategies(PlacementStrategy... strategies) {
        for (PlacementStrategy s : strategies) {
            byType.put(s.structureType(), s);
        }
    }

    /** Road, settlement and city with the normal rules. */
    public static PlacementStrategies standard() {
        return new PlacementStrategies(new RoadPlacement(), new SettlementPlacement(), new CityPlacement());
    }

    public PlacementStrategy get(StructureType type) {
        PlacementStrategy s = byType.get(type);
        if (s == null) {
            throw new IllegalArgumentException("No placement rule for " + type);
        }
        return s;
    }
}
