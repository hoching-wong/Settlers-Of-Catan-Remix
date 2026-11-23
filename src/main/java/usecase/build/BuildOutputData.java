package usecase.build;

import usecase.boardstate.BoardSnapshot;
import java.util.Map;

/**
 * The result of a successful build.
 *
 * @param structure          "ROAD", "SETTLEMENT" or "CITY"
 * @param playerName         who built it
 * @param victoryPoints      the builder's points after the build
 * @param resources          the builder's resources after paying, by resource name
 * @param longestRoadHolder  who holds Longest Road now, or null
 * @param gameOver           whether this build ended the game
 * @param winnerName         the winner if the game is over, otherwise null
 * @param board              the board after the build, for redrawing
 */
public record BuildOutputData(String structure, int locationId, String playerName, int victoryPoints,
                              Map<String, Integer> resources, String longestRoadHolder,
                              boolean gameOver, String winnerName, BoardSnapshot board) {
    public BuildOutputData {
        resources = Map.copyOf(resources);
    }
}
