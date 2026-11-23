package entity;

import java.util.ArrayList;
import java.util.List;

/**
 * The position of a hex tile on the board, in axial coordinates (q, r).
 *
 * <p>The standard board is every hex within distance 2 of the centre (0, 0), which gives 19 tiles.
 * {@link #standardBoard()} lists them row by row (r = -2 to 2, then q from left to right). This
 * order is the tile-id order: tile 0 is {@code standardBoard().get(0)}, tile 9 is the centre,
 * and so on. {@link BoardFactory} builds tiles in this order and the UI uses it to know where to
 * draw each tile.
 */
public record HexCoordinate(int q, int r) {
    /** How far the outer ring is from the centre. */
    public static final int STANDARD_RADIUS = 2;

    private static final List<HexCoordinate> STANDARD = buildStandard();

    /** Id of the middle tile of the standard board, at (0, 0). The desert always goes here. */
    public static final int CENTRE_TILE_ID = STANDARD.indexOf(new HexCoordinate(0, 0));

    /** The 19 coordinates of the standard board, in tile-id order. */
    public static List<HexCoordinate> standardBoard() {
        return STANDARD;
    }

    /** The coordinate of the tile with the given id on a standard board. */
    public static HexCoordinate ofStandardTile(int tileId) {
        if (tileId < 0 || tileId >= STANDARD.size()) {
            throw new IllegalArgumentException("No standard tile with id " + tileId);
        }
        return STANDARD.get(tileId);
    }

    /** Whether the two hexes share a side. */
    public boolean isAdjacentTo(HexCoordinate other) {
        return distanceTo(other) == 1;
    }

    /** Number of steps between the two hexes. */
    public int distanceTo(HexCoordinate other) {
        int dq = q - other.q;
        int dr = r - other.r;
        return (Math.abs(dq) + Math.abs(dr) + Math.abs(dq + dr)) / 2;
    }

    private static List<HexCoordinate> buildStandard() {
        List<HexCoordinate> coords = new ArrayList<>();
        for (int r = -STANDARD_RADIUS; r <= STANDARD_RADIUS; r++) {
            for (int q = -STANDARD_RADIUS; q <= STANDARD_RADIUS; q++) {
                if (Math.abs(q + r) <= STANDARD_RADIUS) {
                    coords.add(new HexCoordinate(q, r));
                }
            }
        }
        return List.copyOf(coords);
    }
}
