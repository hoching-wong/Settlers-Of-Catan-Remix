package catan.entity;

import java.util.EnumMap;
import java.util.Map;

/** A player's resource inventory. Counts are never negative. */
public class ResourceHand {
    private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);

    public ResourceHand() {
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0);
        }
    }

    public void addResource(ResourceType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative: " + amount);
        }
        resources.merge(type, amount, Integer::sum);
    }

    public void removeResource(ResourceType type, int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount must not be negative: " + amount);
        }
        if (resources.get(type) < amount) {
            throw new IllegalArgumentException("Not enough " + type);
        }
        resources.merge(type, -amount, Integer::sum);
    }

    public int getResourceCount(ResourceType type) {
        return resources.get(type);
    }

    public boolean canAfford(Map<ResourceType, Integer> cost) {
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            if (resources.get(e.getKey()) < e.getValue()) {
                return false;
            }
        }
        return true;
    }

    /** Deducts the cost. Throws and changes nothing if the hand cannot afford it. */
    public void spendResources(Map<ResourceType, Integer> cost) {
        if (!canAfford(cost)) {
            throw new IllegalArgumentException("Cannot afford cost: " + cost);
        }
        for (Map.Entry<ResourceType, Integer> e : cost.entrySet()) {
            removeResource(e.getKey(), e.getValue());
        }
    }

    public int getTotalResources() {
        int total = 0;
        for (int count : resources.values()) {
            total += count;
        }
        return total;
    }
}
