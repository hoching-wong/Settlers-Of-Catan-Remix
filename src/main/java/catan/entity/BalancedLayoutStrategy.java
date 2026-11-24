package catan.entity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Desert in the centre, random terrain and tokens around it, but the "red" numbers 6 and 8 (the most likely rolls) never sit on
 * neighbouring tiles. Without this, one corner of the board can be far richer than the rest and
 * whoever settles there first usually wins.
 *
 * <p>The terrain is shuffled once, then the tokens are reshuffled until the rule holds. A valid
 * order is found within a few tries in practice.
 */
public class BalancedLayoutStrategy implements BoardLayoutStrategy {
    /** Safety limit so a broken rule can never loop forever. */
    static final int MAX_ATTEMPTS = 10_000;

    @Override
    public BoardLayout createLayout(Random random) {
        Objects.requireNonNull(random, "random");
        List<TerrainType> terrains = StandardPieces.shuffledTerrainsWithDesertInCentre(random);
        List<Integer> tokens = StandardPieces.tokenPool();
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            Collections.shuffle(tokens, random);
            BoardLayout layout = BoardLayout.assignTokens(terrains, tokens);
            if (!hasAdjacentRedNumbers(layout)) {
                return layout;
            }
        }
        throw new IllegalStateException("Could not find a balanced layout");
    }

    /** Whether two tiles that share a side both have a 6 or an 8. */
    public static boolean hasAdjacentRedNumbers(BoardLayout layout) {
        List<HexCoordinate> coords = HexCoordinate.standardBoard();
        for (int a = 0; a < layout.size(); a++) {
            if (!isRed(layout.numberTokenAt(a))) {
                continue;
            }
            for (int b = a + 1; b < layout.size(); b++) {
                if (isRed(layout.numberTokenAt(b)) && coords.get(a).isAdjacentTo(coords.get(b))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 6 and 8 are the most common rolls after 7. */
    public static boolean isRed(int token) {
        return token == 6 || token == 8;
    }
}
