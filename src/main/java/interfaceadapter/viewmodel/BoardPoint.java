package interfaceadapter.viewmodel;

/**
 * A point on the board in board units, where 1 unit is the distance from a hex's centre to its
 * corner and (0, 0) is the centre of the board. The Swing view scales these to pixels.
 */
public record BoardPoint(double x, double y) {
    public BoardPoint midpoint(BoardPoint other) {
        return new BoardPoint((x + other.x) / 2, (y + other.y) / 2);
    }
}
