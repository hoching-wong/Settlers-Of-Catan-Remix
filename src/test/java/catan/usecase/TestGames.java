package catan.usecase;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.Settlement;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Builds games in a known state for use case tests. */
public final class TestGames {
    private TestGames() {
    }

    /** A game still in setup (nobody has placed anything). */
    public static Game setupGame(long seed, String... names) {
        List<Player> players = new ArrayList<>();
        for (String n : names) {
            players.add(new Player(n));
        }
        Game game = new Game(BoardFactory.createStandardBoard(new Random(seed)), players, WeatherData.none(),
                new Dice(new Random(seed)));
        game.startGame();
        return game;
    }

    /** Setup finished (each player has 2 settlements and 2 roads), Player 1 to move, not rolled. */
    public static Game playingGame(long seed, String... names) {
        Game game = setupGame(seed, names);
        Board board = game.getBoard();
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex v = board.getVertices().stream()
                    .filter(x -> board.isValidSettlementLocation(x, p, true)).findFirst().orElseThrow();
            board.placeBuilding(v, new Settlement(p, v));
            game.setLastPlacedSettlement(v);
            game.advanceSetup();
            Edge e = board.getEdges().stream()
                    .filter(x -> board.isValidRoadLocation(x, p, game.getLastPlacedSettlement()))
                    .findFirst().orElseThrow();
            board.placeRoad(e, new Road(p, e));
            game.advanceSetup();
        }
        return game;
    }

    public static void give(Player p, int each) {
        for (ResourceType t : ResourceType.values()) {
            p.getResources().addResource(t, each);
        }
    }
}
