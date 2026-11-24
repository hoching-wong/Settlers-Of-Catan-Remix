package catan.entity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Puts the desert in the centre, shuffles the other terrain hexes around it, shuffles the number
 * tokens and deals them out to the non-desert tiles. Nothing else is checked, so 6 and 8 may end
 * up side by side.
 */
public class RandomLayoutStrategy implements BoardLayoutStrategy {
    @Override
    public BoardLayout createLayout(Random random) {
        Objects.requireNonNull(random, "random");
        List<TerrainType> terrains = StandardPieces.shuffledTerrainsWithDesertInCentre(random);
        List<Integer> tokens = StandardPieces.tokenPool();
        Collections.shuffle(tokens, random);
        return BoardLayout.assignTokens(terrains, tokens);
    }
}
