package catan.entity;

public class Settlement extends Building {
    public Settlement(Player owner, Vertex location) {
        super(owner, location);
    }

    @Override
    public int getVictoryPoints() {
        return 1;
    }
}
