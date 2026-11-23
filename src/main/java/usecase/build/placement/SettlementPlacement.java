package usecase.build.placement;

import entity.Board;
import entity.Player;
import entity.Settlement;
import entity.StructureType;
import entity.Vertex;

/**
 * A settlement goes on an empty corner that touches one of the player's roads and is at least
 * 2 edges from every other settlement or city.
 */
public class SettlementPlacement implements PlacementStrategy {
    @Override
    public StructureType structureType() {
        return StructureType.SETTLEMENT;
    }

    @Override
    public LocationKind locationKind() {
        return LocationKind.VERTEX;
    }

    @Override
    public boolean isValidLocation(Board board, Player player, int vertexId) {
        if (vertexId < 0 || vertexId >= board.getVertices().size()) {
            return false;
        }
        return board.isValidSettlementLocation(board.getVertex(vertexId), player, false);
    }

    @Override
    public void place(Board board, Player player, int vertexId) {
        Vertex vertex = board.getVertex(vertexId);
        board.placeBuilding(vertex, new Settlement(player, vertex));
    }

    @Override
    public String invalidLocationMessage() {
        return "A settlement must go on an empty corner touching your road, "
                + "at least 2 edges away from any other settlement or city.";
    }
}
