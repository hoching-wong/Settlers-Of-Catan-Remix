package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * What goes on each tile of a new board: its terrain and its number token, in tile-id order.
 * A layout is only data. {@link BoardLayoutStrategy} decides it and {@link BoardFactory} turns it
 * into a {@link Board}.
 */
public final class BoardLayout {
    private final List<TerrainType> terrains;
    private final List<Integer> numberTokens;

    /**
     * @param terrains     one terrain per tile, in tile-id order
     * @param numberTokens one token per tile, in the same order; {@link Tile#NO_TOKEN} for the desert
     */
    public BoardLayout(List<TerrainType> terrains, List<Integer> numberTokens) {
        Objects.requireNonNull(terrains, "terrains");
        Objects.requireNonNull(numberTokens, "numberTokens");
        if (terrains.size() != numberTokens.size()) {
            throw new IllegalArgumentException("Need one number token per terrain: "
                    + terrains.size() + " terrains, " + numberTokens.size() + " tokens");
        }
        for (int i = 0; i < terrains.size(); i++) {
            boolean desert = terrains.get(i) == TerrainType.DESERT;
            boolean noToken = numberTokens.get(i) == Tile.NO_TOKEN;
            if (desert != noToken) {
                throw new IllegalArgumentException("Tile " + i + ": only the desert has no number token");
            }
        }
        this.terrains = List.copyOf(terrains);
        this.numberTokens = List.copyOf(numberTokens);
    }

    /**
     * Builds a layout by giving the tokens, in order, to every tile that is not the desert.
     * This is the usual way a strategy combines a terrain order with a token order.
     */
    public static BoardLayout assignTokens(List<TerrainType> terrains, List<Integer> tokensForResourceTiles) {
        List<Integer> perTile = new ArrayList<>();
        int next = 0;
        for (TerrainType terrain : terrains) {
            if (terrain == TerrainType.DESERT) {
                perTile.add(Tile.NO_TOKEN);
            } else {
                if (next >= tokensForResourceTiles.size()) {
                    throw new IllegalArgumentException("Not enough number tokens for the resource tiles");
                }
                perTile.add(tokensForResourceTiles.get(next++));
            }
        }
        if (next != tokensForResourceTiles.size()) {
            throw new IllegalArgumentException("More number tokens than resource tiles");
        }
        return new BoardLayout(terrains, perTile);
    }

    public int size() {
        return terrains.size();
    }

    public TerrainType terrainAt(int tileId) {
        return terrains.get(tileId);
    }

    public int numberTokenAt(int tileId) {
        return numberTokens.get(tileId);
    }

    public List<TerrainType> getTerrains() {
        return terrains;
    }

    public List<Integer> getNumberTokens() {
        return numberTokens;
    }
}
