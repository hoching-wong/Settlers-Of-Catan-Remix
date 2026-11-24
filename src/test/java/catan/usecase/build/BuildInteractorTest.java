package catan.usecase.build;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.Building;
import catan.entity.City;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Settlement;
import catan.entity.StructureType;
import catan.entity.Vertex;
import catan.usecase.FakeGameRepository;
import catan.usecase.TestGames;
import catan.usecase.boardstate.BoardSnapshot;
import catan.usecase.build.placement.PlacementStrategies;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BuildInteractorTest {
    private final FakeGameRepository repo = new FakeGameRepository();
    private final Recorder presenter = new Recorder();
    private final BuildInteractor interactor = new BuildInteractor(repo, presenter);
    private final PlacementStrategies placements = PlacementStrategies.standard();

    private static class Recorder implements BuildOutputBoundary {
        BuildOutputData success;
        String error;

        @Override
        public void prepareSuccessView(BuildOutputData outputData) {
            success = outputData;
            error = null;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
            success = null;
        }
    }

    /** A game in regular play where Ann has rolled and holds 5 of every resource. */
    private Game readyGame() {
        Game game = TestGames.playingGame(4, "Ann", "Bob");
        game.markRolled();
        TestGames.give(game.getCurrentPlayer(), 5);
        repo.game = game;
        return game;
    }

    private int firstValid(Game game, StructureType type) {
        return placements.get(type).validLocations(game.getBoard(), game.getCurrentPlayer()).get(0);
    }

    /** What a failed build must not change: the whole hand, the roads, the buildings and the points. */
    private record PlayerState(Map<ResourceType, Integer> hand, int roads, int buildings, int victoryPoints) {
        static PlayerState of(Player p) {
            Map<ResourceType, Integer> hand = new EnumMap<>(ResourceType.class);
            for (ResourceType type : ResourceType.values()) {
                hand.put(type, p.getResources().getResourceCount(type));
            }
            return new PlayerState(hand, p.getRoads().size(), p.getBuildings().size(), p.getVictoryPoints());
        }
    }

    private static void assertUnchanged(PlayerState before, Player p) {
        assertEquals(before, PlayerState.of(p), "a failed build changed " + p.getName() + "'s state");
    }

    @Test
    void noGameFails() {
        interactor.execute(new BuildInputData(StructureType.ROAD, 0));
        assertEquals("No game is in progress.", presenter.error);
    }

    @Test
    void mustRollFirst() {
        Game game = TestGames.playingGame(4, "Ann", "Bob");
        TestGames.give(game.getCurrentPlayer(), 5);
        repo.game = game;
        PlayerState before = PlayerState.of(game.getCurrentPlayer());
        interactor.execute(new BuildInputData(StructureType.ROAD, firstValid(game, StructureType.ROAD)));
        assertEquals("Roll the dice before building.", presenter.error);
        assertUnchanged(before, game.getCurrentPlayer());
    }

    @Test
    void buildsARoadAndPays() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        int edge = firstValid(game, StructureType.ROAD);
        interactor.execute(new BuildInputData(StructureType.ROAD, edge));

        assertNull(presenter.error);
        assertTrue(game.getBoard().getEdge(edge).isOccupied());
        assertSame(ann, game.getBoard().getEdge(edge).getRoad().getOwner());
        assertEquals(3, ann.getRoads().size());
        assertEquals(4, ann.getResources().getResourceCount(ResourceType.LUMBER));
        assertEquals(4, ann.getResources().getResourceCount(ResourceType.BRICK));
        assertEquals(5, ann.getResources().getResourceCount(ResourceType.ORE));
        assertEquals("ROAD", presenter.success.structure());
        assertEquals("Ann", presenter.success.playerName());
        assertEquals(4, presenter.success.resources().get("LUMBER"));
        assertEquals(0, presenter.success.board().edges().get(edge).ownerIndex());
        assertEquals(1, repo.gameSaves);
    }

    @Test
    void invalidLocationIsRejectedAndNothingIsSpent() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        // an edge far from Ann's network: one that is not valid
        List<Integer> valid = placements.get(StructureType.ROAD).validLocations(game.getBoard(), ann);
        int invalid = 0;
        while (valid.contains(invalid) || game.getBoard().getEdge(invalid).isOccupied()) {
            invalid++;
        }
        PlayerState before = PlayerState.of(ann);
        interactor.execute(new BuildInputData(StructureType.ROAD, invalid));
        assertTrue(presenter.error.startsWith("A road must go on an empty edge"));
        assertUnchanged(before, ann);
        assertFalse(game.getBoard().getEdge(invalid).isOccupied());
    }

    @Test
    void outOfRangeIdIsJustInvalid() {
        Player ann = readyGame().getCurrentPlayer();
        PlayerState before = PlayerState.of(ann);
        interactor.execute(new BuildInputData(StructureType.SETTLEMENT, 999));
        assertTrue(presenter.error.startsWith("A settlement must go"));
        interactor.execute(new BuildInputData(StructureType.ROAD, -5));
        assertTrue(presenter.error.startsWith("A road must go"));
        assertUnchanged(before, ann);
    }

    @Test
    void cannotAfford() {
        Game game = TestGames.playingGame(4, "Ann", "Bob");
        game.markRolled();
        repo.game = game;
        PlayerState before = PlayerState.of(game.getCurrentPlayer());
        interactor.execute(new BuildInputData(StructureType.SETTLEMENT, 0));
        assertEquals("Not enough resources. A settlement costs 1 lumber, 1 brick, 1 grain, 1 wool.", presenter.error);
        assertUnchanged(before, game.getCurrentPlayer());
    }

    @Test
    void settlementAfterExtendingTheRoad() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        // build roads until a settlement spot opens up
        while (placements.get(StructureType.SETTLEMENT).validLocations(game.getBoard(), ann).isEmpty()) {
            interactor.execute(new BuildInputData(StructureType.ROAD, firstValid(game, StructureType.ROAD)));
            assertNull(presenter.error);
        }
        int vertex = firstValid(game, StructureType.SETTLEMENT);
        int pointsBefore = ann.getVictoryPoints();
        interactor.execute(new BuildInputData(StructureType.SETTLEMENT, vertex));
        assertNull(presenter.error);
        assertTrue(game.getBoard().getVertex(vertex).getBuilding() instanceof Settlement);
        assertEquals(pointsBefore + 1, presenter.success.victoryPoints());
        assertEquals(BoardSnapshot.SETTLEMENT, presenter.success.board().vertices().get(vertex).building());
    }

    @Test
    void cityUpgradesOwnSettlement() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        Vertex own = ann.getBuildings().get(0).getLocation();
        interactor.execute(new BuildInputData(StructureType.CITY, own.getId()));
        assertNull(presenter.error);
        assertTrue(own.getBuilding() instanceof City);
        assertEquals(3, ann.getVictoryPoints());
        assertEquals(3, ann.getResources().getResourceCount(ResourceType.GRAIN));
        assertEquals(2, ann.getResources().getResourceCount(ResourceType.ORE));
        assertEquals(BoardSnapshot.CITY, presenter.success.board().vertices().get(own.getId()).building());
    }

    @Test
    void cityOnOpponentOrEmptyVertexIsRejected() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        Player bob = game.getPlayers().get(1);
        PlayerState annBefore = PlayerState.of(ann);
        PlayerState bobBefore = PlayerState.of(bob);
        int bobs = bob.getBuildings().get(0).getLocation().getId();
        interactor.execute(new BuildInputData(StructureType.CITY, bobs));
        assertEquals("A city can only be built by upgrading one of your own settlements.", presenter.error);
        int empty = 0;
        while (game.getBoard().getVertex(empty).isOccupied()) {
            empty++;
        }
        interactor.execute(new BuildInputData(StructureType.CITY, empty));
        assertNotNull(presenter.error);
        assertUnchanged(annBefore, ann);
        assertUnchanged(bobBefore, bob);
    }

    @Test
    void reachingTenPointsEndsTheGame() {
        Game game = readyGame();
        Player ann = game.getCurrentPlayer();
        Board board = game.getBoard();
        // give Ann extra settlements far from everything until she has 9 points
        for (Vertex v : board.getVertices()) {
            if (ann.getVictoryPoints() >= 9) {
                break;
            }
            if (board.isValidSettlementLocation(v, ann, true)) {
                board.placeBuilding(v, new Settlement(ann, v));
            }
        }
        assertEquals(9, ann.getVictoryPoints());
        Building settlement = ann.getBuildings().get(0);
        interactor.execute(new BuildInputData(StructureType.CITY, settlement.getLocation().getId()));
        assertTrue(presenter.success.gameOver());
        assertEquals("Ann", presenter.success.winnerName());
        assertEquals(GamePhase.GAME_OVER, game.getPhase());

        interactor.execute(new BuildInputData(StructureType.ROAD, 0));
        assertEquals("The game is over.", presenter.error);
    }
}
