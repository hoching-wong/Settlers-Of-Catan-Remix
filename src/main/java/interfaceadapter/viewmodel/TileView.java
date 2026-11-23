package interfaceadapter.viewmodel;

import java.util.List;

/**
 * Everything the view needs to draw one hex.
 *
 * @param terrain     terrain name, for example "FOREST"
 * @param label       what the hex produces, for example "Lumber", or "Desert"
 * @param numberToken 2-12, or 0 for the desert
 * @param pips        how many dots go under the number (more dots = rolled more often)
 * @param red         true for 6 and 8, which are drawn in red
 */
public record TileView(int id, BoardPoint centre, List<BoardPoint> corners, String terrain, String label,
                       int numberToken, int pips, boolean red) {
    public TileView {
        corners = List.copyOf(corners);
    }

    public boolean hasNumberToken() {
        return numberToken != 0;
    }
}
