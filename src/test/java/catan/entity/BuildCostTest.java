package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class BuildCostTest {
    @Test
    void roadCost() {
        assertEquals(Map.of(ResourceType.LUMBER, 1, ResourceType.BRICK, 1), BuildCost.of(StructureType.ROAD));
    }

    @Test
    void settlementCost() {
        assertEquals(Map.of(ResourceType.LUMBER, 1, ResourceType.BRICK, 1,
                ResourceType.WOOL, 1, ResourceType.GRAIN, 1), BuildCost.of(StructureType.SETTLEMENT));
    }

    @Test
    void cityCost() {
        assertEquals(Map.of(ResourceType.GRAIN, 2, ResourceType.ORE, 3), BuildCost.of(StructureType.CITY));
    }

    @Test
    void costIsUnmodifiable() {
        assertThrows(UnsupportedOperationException.class,
                () -> BuildCost.of(StructureType.ROAD).put(ResourceType.ORE, 1));
    }
}
