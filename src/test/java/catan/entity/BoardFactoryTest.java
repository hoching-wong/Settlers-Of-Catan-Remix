package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;

class BoardFactoryTest {
    private final Board board = BoardFactory.createStandardBoard(new Random(42));

    @Test
    void hasStandardCounts() {
        assertEquals(19, board.getTiles().size());
        assertEquals(54, board.getVertices().size());
        assertEquals(72, board.getEdges().size());
    }

    @Test
    void idsAreSequentialFromZero() {
        for (int i = 0; i < board.getTiles().size(); i++) {
            assertEquals(i, board.getTiles().get(i).getId());
        }
        for (int i = 0; i < board.getVertices().size(); i++) {
            assertEquals(i, board.getVertices().get(i).getId());
        }
        for (int i = 0; i < board.getEdges().size(); i++) {
            assertEquals(i, board.getEdges().get(i).getId());
        }
    }

    @Test
    void terrainDistributionIsStandard() {
        Map<TerrainType, Integer> counts = new EnumMap<>(TerrainType.class);
        for (Tile t : board.getTiles()) {
            counts.merge(t.getTerrain(), 1, Integer::sum);
        }
        assertEquals(4, counts.get(TerrainType.FOREST));
        assertEquals(3, counts.get(TerrainType.HILLS));
        assertEquals(4, counts.get(TerrainType.FIELDS));
        assertEquals(4, counts.get(TerrainType.PASTURE));
        assertEquals(3, counts.get(TerrainType.MOUNTAINS));
        assertEquals(1, counts.get(TerrainType.DESERT));
    }

    @Test
    void numberTokensAreStandardAndDesertHasNone() {
        List<Integer> tokens = new ArrayList<>();
        for (Tile t : board.getTiles()) {
            if (t.getTerrain() == TerrainType.DESERT) {
                assertFalse(t.hasNumberToken());
                assertNull(t.getResourceType());
            } else {
                assertNotNull(t.getResourceType());
                tokens.add(t.getNumberToken());
            }
        }
        tokens.sort(null);
        assertEquals(List.of(2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12), tokens);
    }

    @Test
    void everyTileHasSixDistinctVerticesAndEdges() {
        for (Tile t : board.getTiles()) {
            assertEquals(6, new HashSet<>(t.getVertices()).size());
            assertEquals(6, new HashSet<>(t.getEdges()).size());
        }
    }

    @Test
    void tileEdgesJoinConsecutiveTileVertices() {
        for (Tile t : board.getTiles()) {
            List<Vertex> vs = t.getVertices();
            for (int i = 0; i < 6; i++) {
                Edge e = t.getEdges().get(i);
                Vertex a = vs.get(i);
                Vertex b = vs.get((i + 1) % 6);
                assertTrue((e.getVertex1() == a && e.getVertex2() == b)
                        || (e.getVertex1() == b && e.getVertex2() == a));
            }
        }
    }

    @Test
    void vertexSharingMatchesHexGeometry() {
        int[] tilesPerVertex = new int[4];
        for (Vertex v : board.getVertices()) {
            tilesPerVertex[board.getTilesAdjacentTo(v).size()]++;
        }
        assertEquals(0, tilesPerVertex[0]);
        assertEquals(18, tilesPerVertex[1]);
        assertEquals(12, tilesPerVertex[2]);
        assertEquals(24, tilesPerVertex[3]);
    }

    @Test
    void edgeSharingMatchesHexGeometry() {
        Map<Edge, Integer> tilesPerEdge = new java.util.HashMap<>();
        for (Tile t : board.getTiles()) {
            for (Edge e : t.getEdges()) {
                tilesPerEdge.merge(e, 1, Integer::sum);
            }
        }
        long perimeter = tilesPerEdge.values().stream().filter(n -> n == 1).count();
        long interior = tilesPerEdge.values().stream().filter(n -> n == 2).count();
        assertEquals(30, perimeter);
        assertEquals(42, interior);
        assertEquals(72, perimeter + interior);
    }

    @Test
    void everyVertexHasTwoOrThreeEdgesAndAdjacencyIsSymmetric() {
        for (Vertex v : board.getVertices()) {
            int degree = v.getAdjacentEdges().size();
            assertTrue(degree == 2 || degree == 3, "vertex " + v.getId() + " has degree " + degree);
            for (Edge e : v.getAdjacentEdges()) {
                assertTrue(e.getVertex1() == v || e.getVertex2() == v);
                assertTrue(e.getOtherVertex(v).getAdjacentVertices().contains(v));
            }
        }
    }

    @Test
    void noDuplicateEdgesBetweenTheSameVertices() {
        Set<Set<Integer>> pairs = new HashSet<>();
        for (Edge e : board.getEdges()) {
            assertTrue(pairs.add(Set.of(e.getVertex1().getId(), e.getVertex2().getId())));
        }
    }

    @Test
    void boardGraphIsConnected() {
        Set<Vertex> seen = new HashSet<>();
        Deque<Vertex> queue = new ArrayDeque<>();
        queue.add(board.getVertex(0));
        seen.add(board.getVertex(0));
        while (!queue.isEmpty()) {
            for (Vertex next : queue.poll().getAdjacentVertices()) {
                if (seen.add(next)) {
                    queue.add(next);
                }
            }
        }
        assertEquals(54, seen.size());
    }

    @Test
    void newBoardHasNothingBuilt() {
        for (Vertex v : board.getVertices()) {
            assertFalse(v.isOccupied());
        }
        for (Edge e : board.getEdges()) {
            assertFalse(e.isOccupied());
        }
    }

    private static String layout(Board b) {
        StringBuilder sb = new StringBuilder();
        for (Tile t : b.getTiles()) {
            sb.append(t.getTerrain()).append(':').append(t.getNumberToken()).append(' ');
        }
        return sb.toString();
    }

    @Test
    void sameSeedGivesSameBoard() {
        assertEquals(layout(BoardFactory.createStandardBoard(new Random(7))),
                layout(BoardFactory.createStandardBoard(new Random(7))));
    }

    @Test
    void differentSeedsGiveDifferentBoards() {
        Set<String> layouts = new HashSet<>();
        for (long seed = 0; seed < 10; seed++) {
            layouts.add(layout(BoardFactory.createStandardBoard(new Random(seed))));
        }
        assertTrue(layouts.size() > 1);
    }
}
