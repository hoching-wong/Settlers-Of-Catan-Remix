package catan.usecase.generateboard;

/**
 * Input for generating a new board.
 *
 * @param seed the random seed, or null for a different board every time. A fixed seed always
 *             gives the same board, which is useful for tests and for replaying a layout.
 */
public record GenerateBoardInputData(Long seed) {
    /** A new random board. */
    public static GenerateBoardInputData random() {
        return new GenerateBoardInputData(null);
    }
}
