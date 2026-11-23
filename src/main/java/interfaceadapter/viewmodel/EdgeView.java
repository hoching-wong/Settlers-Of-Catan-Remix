package interfaceadapter.viewmodel;

/** @param ownerIndex index of the player whose road is here, or -1 when empty */
public record EdgeView(int id, BoardPoint from, BoardPoint to, int ownerIndex) {
    public BoardPoint midpoint() {
        return from.midpoint(to);
    }

    public boolean hasRoad() {
        return ownerIndex >= 0;
    }
}
