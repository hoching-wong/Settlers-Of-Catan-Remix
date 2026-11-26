package catan.entity;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Works out the Longest Road. Stateless: it never changes a player or the board. */
public class LongestRoadCalculator {
    /** The fewest continuous segments that qualify for Longest Road. */
    public static final int MIN_ROAD_LENGTH = 5;

    /**
     * The longest continuous road of the player: the most of their roads that can be walked
     * in one path without using any road twice. Paths may branch off and loop back through a
     * vertex, but cannot pass through a vertex that holds another player's settlement or city.
     * A path may still start or end at such a vertex. The player's own buildings never break
     * a road.
     */
    public static int longestRoadOf(Player player, Board board) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(board, "board");

        int total = 0;
        Set<Vertex> starts = new LinkedHashSet<>();
        for (Edge edge : board.getEdges()) {
            if (ownsRoad(edge, player)) {
                total++;
                starts.add(edge.getVertex1());
                starts.add(edge.getVertex2());
            }
        }
        Search search = new Search(player, total);
        for (Vertex start : starts) {
            search.from(start, 0);
            if (search.best == total) {
                break;
            }
        }
        return search.best;
    }

    /**
     * The player who should hold Longest Road, or null if nobody should.
     *
     * <ul>
     *   <li>The holder needs at least {@link #MIN_ROAD_LENGTH} segments.
     *   <li>The longest road wins. A tie with the current holder keeps the holder.
     *   <li>If the current holder no longer qualifies or is beaten, the title goes to the
     *       single player with the longest road, or to nobody if several other players tie.
     * </ul>
     */
    public static Player findHolder(List<Player> players, Board board, Player currentHolder) {
        Objects.requireNonNull(players, "players");
        Objects.requireNonNull(board, "board");

        int longest = 0;
        Player leader = null;
        boolean tied = false;
        int holderLength = 0;
        for (Player p : players) {
            int length = longestRoadOf(p, board);
            if (p == currentHolder) {
                holderLength = length;
            }
            if (length > longest) {
                longest = length;
                leader = p;
                tied = false;
            } else if (length == longest && length > 0) {
                tied = true;
            }
        }
        if (longest < MIN_ROAD_LENGTH) {
            return null;
        }
        if (currentHolder != null && holderLength == longest) {
            return currentHolder;
        }
        return tied ? null : leader;
    }

    private static boolean ownsRoad(Edge edge, Player player) {
        return edge.isOccupied() && edge.getRoad().getOwner() == player;
    }

    /** Depth-first search over the player's roads, using each road at most once per path. */
    private static final class Search {
        private final Player player;
        private final int totalRoads;
        private final Set<Edge> used = new HashSet<>();
        int best;

        Search(Player player, int totalRoads) {
            this.player = player;
            this.totalRoads = totalRoads;
        }

        private boolean blocks(Vertex vertex) {
            return vertex.isOccupied() && vertex.getBuilding().getOwner() != player;
        }

        void from(Vertex vertex, int length) {
            best = Math.max(best, length);
            for (Edge edge : vertex.getAdjacentEdges()) {
                if (best == totalRoads) {
                    return; // every road is already in one path, nothing can beat it
                }
                if (!ownsRoad(edge, player) || used.contains(edge)) {
                    continue;
                }
                used.add(edge);
                Vertex next = edge.getOtherVertex(vertex);
                if (blocks(next)) {
                    best = Math.max(best, length + 1); // the path ends at the blocked vertex
                } else {
                    from(next, length + 1);
                }
                used.remove(edge);
            }
        }
    }
}
