package catan.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Calculates the longest continuous road and determines its holder. */
public final class LongestRoadCalculator {

  /** Minimum number of roads required for the Longest Road title. */
  public static final int MIN_ROAD_LENGTH = 5;

  /** Prevents creating an instance of this utility class. */
  private LongestRoadCalculator() {}

  /**
   * Calculates a player's longest continuous road.
   *
   * @param player the player whose roads are being checked
   * @param board the game board
   * @return the number of roads in the longest continuous path
   */
  public static int longestRoadOf(Player player, Board board) {
    Objects.requireNonNull(player);
    Objects.requireNonNull(board);

    int longest = 0;

    for (Edge edge : board.getEdges()) {
      if (ownsRoad(edge, player)) {
        int fromFirst = dfs(player, edge.getVertex1(), new HashSet<>());

        int fromSecond = dfs(player, edge.getVertex2(), new HashSet<>());

        longest = Math.max(longest, Math.max(fromFirst, fromSecond));
      }
    }

    return longest;
  }

  /**
   * Determines which player should hold the Longest Road title.
   *
   * @param players the players in the game
   * @param board the game board
   * @param currentHolder the current holder, or null
   * @return the eligible holder, or null if no player qualifies
   */
  public static Player findHolder(List<Player> players, Board board, Player currentHolder) {

    Objects.requireNonNull(players);
    Objects.requireNonNull(board);

    int longest = 0;
    int currentHolderLength = 0;
    Player leader = null;
    boolean tied = false;

    for (Player player : players) {
      int length = longestRoadOf(player, board);

      if (player == currentHolder) {
        currentHolderLength = length;
      }

      if (length > longest) {
        longest = length;
        leader = player;
        tied = false;
      } else if (length == longest && length > 0) {
        tied = true;
      }
    }

    if (longest < MIN_ROAD_LENGTH) {
      return null;
    }

    if (currentHolder != null && currentHolderLength == longest) {
      return currentHolder;
    }

    if (tied) {
      return null;
    }

    return leader;
  }

  /**
   * Checks whether an edge contains the player's road.
   *
   * @param edge the edge to check
   * @param player the player
   * @return true if the road belongs to the player
   */
  private static boolean ownsRoad(Edge edge, Player player) {
    return edge.isOccupied() && edge.getRoad().getOwner() == player;
  }

  /**
   * Finds the longest path starting from a vertex.
   *
   * @param player the player
   * @param current the current vertex
   * @param usedRoads the roads already used in this path
   * @return the maximum number of additional roads
   */
  private static int dfs(Player player, Vertex current, Set<Edge> usedRoads) {

    if (!usedRoads.isEmpty()
        && current.isOccupied()
        && current.getBuilding().getOwner() != player) {
      return 0;
    }

    int longest = 0;

    for (Edge edge : current.getAdjacentEdges()) {
      if (!ownsRoad(edge, player) || usedRoads.contains(edge)) {
        continue;
      }

      usedRoads.add(edge);

      Vertex next = edge.getOtherVertex(current);

      int length = 1 + dfs(player, next, usedRoads);

      longest = Math.max(longest, length);

      usedRoads.remove(edge);
    }

    return longest;
  }
}
