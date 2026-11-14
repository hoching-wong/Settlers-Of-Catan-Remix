package catan.entity;

import java.util.Objects;

public class Road {
    private final Player owner;
    private final Edge location;

    public Road(Player owner, Edge location) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.location = Objects.requireNonNull(location, "location");
    }

    public Player getOwner() {
        return owner;
    }

    public Edge getLocation() {
        return location;
    }
}
