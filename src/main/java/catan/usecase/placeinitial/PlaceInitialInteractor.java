package catan.usecase.placeinitial;

import catan.entity.Board;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.SetupStep;
import catan.entity.Settlement;
import catan.entity.Tile;
import catan.entity.Vertex;
import catan.usecase.port.GameRepository;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class PlaceInitialInteractor implements PlaceInitialInputBoundary {
    private final GameRepository gameRepository;
    private final PlaceInitialOutputBoundary outputBoundary;

    public PlaceInitialInteractor(GameRepository gameRepository, PlaceInitialOutputBoundary outputBoundary) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
    }

    @Override
    public void placeInitial(PlaceInitialInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

        Game game = gameRepository.get();
        if (game == null) {
            outputBoundary.presentError("No game is in progress.");
            return;
        }
        if (game.getPhase() == GamePhase.GAME_OVER) {
            outputBoundary.presentError("The game is over.");
            return;
        }
        if (game.getPhase() != GamePhase.SETUP) {
            outputBoundary.presentError("Initial placement is over.");
            return;
        }

        if (game.getSetupStep() == SetupStep.SETTLEMENT) {
            placeSettlement(game, inputData.locationId());
        } else {
            placeRoad(game, inputData.locationId());
        }
    }

    private void placeSettlement(Game game, int vertexId) {
        Board board = game.getBoard();
        Player player = game.getCurrentPlayer();
        Vertex vertex = findVertex(board, vertexId);
        if (vertex == null) {
            outputBoundary.presentError("That is not a place on the board.");
            return;
        }
        if (vertex.isOccupied()) {
            outputBoundary.presentError("That spot is already taken.");
            return;
        }
        if (!board.isValidSettlementLocation(vertex, player, true)) {
            outputBoundary.presentError(
                    "A settlement must be at least 2 edges away from any other settlement or city.");
            return;
        }

        board.placeBuilding(vertex, new Settlement(player, vertex));
        Map<ResourceType, Integer> gained = isSecondSettlement(game)
                ? giveStartingResources(board, vertex, player)
                : Map.of();
        game.setLastPlacedSettlement(vertex);
        game.advanceSetup();

        outputBoundary.presentPlacement(new PlaceInitialOutputData(
                SetupStep.SETTLEMENT, player.getName(), vertexId, gained,
                false, game.getCurrentPlayer().getName(), game.getSetupStep()));
    }

    private void placeRoad(Game game, int edgeId) {
        Board board = game.getBoard();
        Player player = game.getCurrentPlayer();
        Edge edge = findEdge(board, edgeId);
        if (edge == null) {
            outputBoundary.presentError("That is not a place on the board.");
            return;
        }
        if (edge.isOccupied()) {
            outputBoundary.presentError("That edge already has a road.");
            return;
        }
        if (!board.isValidRoadLocation(edge, player, game.getLastPlacedSettlement())) {
            outputBoundary.presentError("The road must touch the settlement you just placed.");
            return;
        }

        board.placeRoad(edge, new Road(player, edge));
        game.advanceSetup();

        boolean setupComplete = game.getPhase() == GamePhase.PLAYING;
        outputBoundary.presentPlacement(new PlaceInitialOutputData(
                SetupStep.ROAD, player.getName(), edgeId, Map.of(),
                setupComplete, game.getCurrentPlayer().getName(), game.getSetupStep()));
    }

    /**
     * Each player's second settlement is placed in the reverse half of the snake order, so the
     * number of completed placement turns is at least the number of players.
     */
    private static boolean isSecondSettlement(Game game) {
        return game.getSetupIndex() >= game.getPlayers().size();
    }

    /** 1 resource for every resource tile touching the settlement. The desert gives nothing. */
    private static Map<ResourceType, Integer> giveStartingResources(Board board, Vertex vertex, Player player) {
        Map<ResourceType, Integer> gained = new EnumMap<>(ResourceType.class);
        for (Tile tile : board.getTilesAdjacentTo(vertex)) {
            ResourceType resource = tile.getResourceType();
            if (resource != null) {
                gained.merge(resource, 1, Integer::sum);
                player.getResources().addResource(resource, 1);
            }
        }
        return Collections.unmodifiableMap(gained);
    }

    private static Vertex findVertex(Board board, int id) {
        try {
            return board.getVertex(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Edge findEdge(Board board, int id) {
        try {
            return board.getEdge(id);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
