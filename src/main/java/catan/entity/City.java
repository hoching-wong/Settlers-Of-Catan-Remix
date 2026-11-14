package catan.entity;

public class City extends Building {
    public City(Player owner, Vertex location) {
        super(owner, location);
    }

    @Override
    public int getVictoryPoints() {
        return 2;
    }
}
