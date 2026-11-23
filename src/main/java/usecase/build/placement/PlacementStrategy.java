package usecase.build.placement;

import entity.Board;
import entity.Player;
import entity.StructureType;
import java.util.ArrayList;
import java.util.List;

/**
 * Strategy pattern: how one kind of structure is checked and put on the board. Each structure
 * (road, settlement, city) has its own class, so the build use case never needs a
 * {@code switch} on the structure type, and a new structure is a new class (Open/Closed).
 *
 * <p>The checks here are only about the location. Turn, dice and cost checks are in
 * {@code usecase.build.rules}.
 */
public interface PlacementStrategy {
    StructureType structureType();

    LocationKind locationKind();

    /** Whether the player may put this structure on the vertex or edge with this id. */
    boolean isValidLocation(Board board, Player player, int locationId);

    /** Puts the structure on the board. Call {@link #isValidLocation} first. */
    void place(Board board, Player player, int locationId);

    /** The message shown when {@link #isValidLocation} is false. */
    String invalidLocationMessage();

    /** Every vertex or edge id the player could build on right now. */
    default List<Integer> validLocations(Board board, Player player) {
        List<Integer> result = new ArrayList<>();
        int count = locationKind() == LocationKind.VERTEX ? board.getVertices().size() : board.getEdges().size();
        for (int id = 0; id < count; id++) {
            if (isValidLocation(board, player, id)) {
                result.add(id);
            }
        }
        return result;
    }
}
