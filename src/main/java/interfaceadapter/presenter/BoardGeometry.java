package interfaceadapter.presenter;

import interfaceadapter.viewmodel.BoardPoint;

/**
 * Where things are on a pointy-top hex board, in board units (hex corner radius = 1).
 * Uses the same formulas as {@code BoardFactory}, so corner i here is corner i of the tile.
 */
public final class BoardGeometry {
    private static final double SQRT3 = Math.sqrt(3);

    private BoardGeometry() {
    }

    /** Centre of the hex at axial (q, r). */
    public static BoardPoint centre(int q, int r) {
        return new BoardPoint(SQRT3 * (q + r / 2.0), 1.5 * r);
    }

    /** Corner i (0-5, clockwise from the upper right) of the hex at axial (q, r). */
    public static BoardPoint corner(int q, int r, int i) {
        BoardPoint c = centre(q, r);
        double angle = Math.toRadians(60 * i - 30);
        return new BoardPoint(c.x() + Math.cos(angle), c.y() + Math.sin(angle));
    }

    /** Dots under a number token: 2 and 12 get 1, 6 and 8 get 5. */
    public static int pips(int numberToken) {
        return numberToken == 0 ? 0 : 6 - Math.abs(7 - numberToken);
    }
}
