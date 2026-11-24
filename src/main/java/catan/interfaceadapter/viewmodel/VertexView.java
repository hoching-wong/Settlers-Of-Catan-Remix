package catan.interfaceadapter.viewmodel;

/**
 * @param building   "NONE", "SETTLEMENT" or "CITY"
 * @param ownerIndex index of the owning player (for the colour), or -1 when empty
 */
public record VertexView(int id, BoardPoint position, String building, int ownerIndex) {
    public boolean isEmpty() {
        return ownerIndex < 0;
    }
}
