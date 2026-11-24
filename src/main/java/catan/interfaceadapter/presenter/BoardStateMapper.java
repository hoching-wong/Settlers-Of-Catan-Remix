package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.BoardPoint;
import catan.interfaceadapter.viewmodel.BoardState;
import catan.interfaceadapter.viewmodel.EdgeView;
import catan.interfaceadapter.viewmodel.TileView;
import catan.interfaceadapter.viewmodel.VertexView;
import catan.usecase.boardstate.BoardSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Turns a {@link BoardSnapshot} (use case data) into drawable tiles, vertices and edges with
 * positions. Shared by every presenter that redraws the board.
 */
public final class BoardStateMapper {
    private BoardStateMapper() {
    }

    /** Replaces the tiles, vertices and edges in the state with the snapshot's. */
    public static void applySnapshot(BoardState state, BoardSnapshot snapshot) {
        List<TileView> tiles = new ArrayList<>();
        Map<Integer, BoardPoint> vertexPositions = new HashMap<>();
        for (BoardSnapshot.TileData t : snapshot.tiles()) {
            List<BoardPoint> corners = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                BoardPoint p = BoardGeometry.corner(t.q(), t.r(), i);
                corners.add(p);
                vertexPositions.putIfAbsent(t.cornerVertexIds().get(i), p);
            }
            tiles.add(new TileView(t.id(), BoardGeometry.centre(t.q(), t.r()), corners, t.terrain(),
                    label(t), t.numberToken(), BoardGeometry.pips(t.numberToken()),
                    t.numberToken() == 6 || t.numberToken() == 8));
        }

        List<VertexView> vertices = new ArrayList<>();
        for (BoardSnapshot.VertexData v : snapshot.vertices()) {
            vertices.add(new VertexView(v.id(), vertexPositions.get(v.id()), v.building(), v.ownerIndex()));
        }

        List<EdgeView> edges = new ArrayList<>();
        for (BoardSnapshot.EdgeData e : snapshot.edges()) {
            edges.add(new EdgeView(e.id(), vertexPositions.get(e.vertex1Id()), vertexPositions.get(e.vertex2Id()),
                    e.ownerIndex()));
        }

        state.setTiles(tiles);
        state.setVertices(vertices);
        state.setEdges(edges);
    }

    /** "Lumber", "Brick", ... or "Desert". */
    static String label(BoardSnapshot.TileData tile) {
        String word = tile.resource() == null ? "DESERT" : tile.resource();
        return word.charAt(0) + word.substring(1).toLowerCase(Locale.ROOT);
    }
}
