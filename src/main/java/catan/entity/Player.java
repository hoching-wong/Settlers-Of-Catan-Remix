package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Player {
    private static final int LONGEST_ROAD_POINTS = 2;

    private final String name;
    private final ResourceHand resources = new ResourceHand();
    private final List<Building> buildings = new ArrayList<>();
    private final List<Road> roads = new ArrayList<>();
    private boolean hasLongestRoad;

    public Player(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    public String getName() {
        return name;
    }

    public void addBuilding(Building building) {
        buildings.add(Objects.requireNonNull(building, "building"));
    }

    public void removeBuilding(Building building) {
        buildings.remove(building);
    }

    public void addRoad(Road road) {
        roads.add(Objects.requireNonNull(road, "road"));
    }

    public ResourceHand getResources() {
        return resources;
    }

    public List<Building> getBuildings() {
        return Collections.unmodifiableList(buildings);
    }

    public List<Road> getRoads() {
        return Collections.unmodifiableList(roads);
    }

    public boolean hasLongestRoad() {
        return hasLongestRoad;
    }

    public void setHasLongestRoad(boolean value) {
        this.hasLongestRoad = value;
    }

    /** Computed from buildings plus 2 for Longest Road. Never stored. */
    public int getVictoryPoints() {
        int points = 0;
        for (Building b : buildings) {
            points += b.getVictoryPoints();
        }
        return hasLongestRoad ? points + LONGEST_ROAD_POINTS : points;
    }
}
