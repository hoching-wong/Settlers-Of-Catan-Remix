package catan.entity;

import java.util.Random;

/**
 * Strategy pattern: one way of deciding where the terrain hexes and number tokens go on a new
 * board. {@link BoardFactory} builds the board the same way whatever strategy is used, so a new
 * rule for random generation is a new class, not a change to the factory.
 *
 * <p>Implementations must use only the given {@code Random}, so the same seed always gives the
 * same layout.
 */
public interface BoardLayoutStrategy {
    /** A layout for the 19 tiles of the standard board, in tile-id order. */
    BoardLayout createLayout(Random random);
}
