package catan.usecase.build.placement;

import catan.entity.Board;
import catan.entity.City;
import catan.entity.Player;
import catan.entity.Settlement;
import catan.entity.StructureType;
import catan.entity.Vertex;

/** A city can only replace one of the player's own settlements. */
public class CityPlacement implements PlacementStrategy {
    @Override
    public StructureType structureType() {
        return StructureType.CITY;
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
        Vertex vertex = board.getVertex(vertexId);
        return vertex.getBuilding() instanceof Settlement && vertex.getBuilding().getOwner() == player;
    }

    @Override
    public void place(Board board, Player player, int vertexId) {
        Vertex vertex = board.getVertex(vertexId);
        board.upgradeToCity(vertex, new City(player, vertex));
    }

    @Override
    public String invalidLocationMessage() {
        return "A city can only be built by upgrading one of your own settlements.";
    }
}
