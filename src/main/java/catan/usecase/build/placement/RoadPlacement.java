package catan.usecase.build.placement;

import catan.entity.Board;
import catan.entity.Edge;
import catan.entity.Player;
import catan.entity.Road;
import catan.entity.StructureType;

/** A road goes on an empty edge that connects to the player's own road, settlement or city. */
public class RoadPlacement implements PlacementStrategy {
    @Override
    public StructureType structureType() {
        return StructureType.ROAD;
    }

    @Override
    public LocationKind locationKind() {
        return LocationKind.EDGE;
    }

    @Override
    public boolean isValidLocation(Board board, Player player, int edgeId) {
        if (edgeId < 0 || edgeId >= board.getEdges().size()) {
            return false;
        }
        return board.isValidRoadLocation(board.getEdge(edgeId), player, null);
    }

    @Override
    public void place(Board board, Player player, int edgeId) {
        Edge edge = board.getEdge(edgeId);
        board.placeRoad(edge, new Road(player, edge));
    }

    @Override
    public String invalidLocationMessage() {
        return "A road must go on an empty edge connected to your own road, settlement or city.";
    }
}
