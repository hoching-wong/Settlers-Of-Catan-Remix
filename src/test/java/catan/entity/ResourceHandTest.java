package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class ResourceHandTest {
    @Test
    void startsEmpty() {
        ResourceHand hand = new ResourceHand();
        for (ResourceType t : ResourceType.values()) {
            assertEquals(0, hand.getResourceCount(t));
        }
        assertEquals(0, hand.getTotalResources());
    }

    @Test
    void addAndRemove() {
        ResourceHand hand = new ResourceHand();
        hand.addResource(ResourceType.ORE, 3);
        hand.addResource(ResourceType.ORE, 2);
        hand.removeResource(ResourceType.ORE, 4);
        assertEquals(1, hand.getResourceCount(ResourceType.ORE));
        assertEquals(1, hand.getTotalResources());
    }

    @Test
    void removeTooManyThrowsAndKeepsCount() {
        ResourceHand hand = new ResourceHand();
        hand.addResource(ResourceType.WOOL, 1);
        assertThrows(IllegalArgumentException.class, () -> hand.removeResource(ResourceType.WOOL, 2));
        assertEquals(1, hand.getResourceCount(ResourceType.WOOL));
    }

    @Test
    void negativeAmountsRejected() {
        ResourceHand hand = new ResourceHand();
        assertThrows(IllegalArgumentException.class, () -> hand.addResource(ResourceType.BRICK, -1));
        assertThrows(IllegalArgumentException.class, () -> hand.removeResource(ResourceType.BRICK, -1));
    }

    @Test
    void canAfford() {
        ResourceHand hand = new ResourceHand();
        hand.addResource(ResourceType.LUMBER, 1);
        hand.addResource(ResourceType.BRICK, 1);
        assertTrue(hand.canAfford(BuildCost.of(StructureType.ROAD)));
        assertFalse(hand.canAfford(BuildCost.of(StructureType.SETTLEMENT)));
        assertTrue(hand.canAfford(Map.of()));
    }

    @Test
    void spendResourcesDeducts() {
        ResourceHand hand = new ResourceHand();
        hand.addResource(ResourceType.LUMBER, 2);
        hand.addResource(ResourceType.BRICK, 1);
        hand.spendResources(BuildCost.of(StructureType.ROAD));
        assertEquals(1, hand.getResourceCount(ResourceType.LUMBER));
        assertEquals(0, hand.getResourceCount(ResourceType.BRICK));
    }

    @Test
    void spendResourcesWhenUnaffordableChangesNothing() {
        ResourceHand hand = new ResourceHand();
        hand.addResource(ResourceType.LUMBER, 1);
        assertThrows(IllegalArgumentException.class,
                () -> hand.spendResources(BuildCost.of(StructureType.ROAD)));
        assertEquals(1, hand.getResourceCount(ResourceType.LUMBER));
    }
}
