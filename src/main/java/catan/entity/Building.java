package catan.entity;

import java.util.Objects;

/** A structure on a vertex. Subclasses define how many victory points it is worth. */
public abstract class Building {
    private final Player owner;
    private final Vertex location;

    protected Building(Player owner, Vertex location) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.location = Objects.requireNonNull(location, "location");
    }

    public Player getOwner() {
        return owner;
    }

    public Vertex getLocation() {
        return location;
    }

    public abstract int getVictoryPoints();
}
