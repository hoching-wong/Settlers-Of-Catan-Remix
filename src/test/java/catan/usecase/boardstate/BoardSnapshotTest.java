package catan.usecase.boardstate;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.Edge;
import catan.entity.HexCoordinate;
import catan.entity.Player;
import catan.entity.Road;
import catan.entity.Settlement;
import catan.entity.Vertex;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class BoardSnapshotTest {
    @Test
    void copiesTilesWithCoordinatesAndCorners() {
        Board board = BoardFactory.createStandardBoard(new Random(8));
        BoardSnapshot snap = BoardSnapshot.of(board, List.of());
        for (BoardSnapshot.TileData t : snap.tiles()) {
            assertEquals(HexCoordinate.ofStandardTile(t.id()), new HexCoordinate(t.q(), t.r()));
            assertEquals(6, t.cornerVertexIds().size());
            assertEquals(board.getTile(t.id()).getVertices().get(0).getId(), t.cornerVertexIds().get(0));
            if ("DESERT".equals(t.terrain())) {
                assertNull(t.resource());
                assertEquals(0, t.numberToken());
            }
        }
    }

    @Test
    void ownersBecomePlayerIndexes() {
        Board board = BoardFactory.createStandardBoard(new Random(8));
        Player ann = new Player("Ann");
        Player bob = new Player("Bob");
        Vertex v = board.getVertex(0);
        board.placeBuilding(v, new Settlement(bob, v));
        board.upgradeToCity(v, new City(bob, v));
        Vertex w = board.getVertex(20);
        board.placeBuilding(w, new Settlement(ann, w));
        Edge e = board.getEdge(5);
        board.placeRoad(e, new Road(ann, e));

        BoardSnapshot snap = BoardSnapshot.of(board, List.of(ann, bob));
        assertEquals(new BoardSnapshot.VertexData(0, BoardSnapshot.CITY, 1), snap.vertices().get(0));
        assertEquals(new BoardSnapshot.VertexData(20, BoardSnapshot.SETTLEMENT, 0), snap.vertices().get(20));
        assertEquals(BoardSnapshot.NONE, snap.vertices().get(1).building());
        assertEquals(0, snap.edges().get(5).ownerIndex());
        assertEquals(BoardSnapshot.NO_OWNER, snap.edges().get(6).ownerIndex());
        assertEquals(e.getVertex1().getId(), snap.edges().get(5).vertex1Id());
    }
}
