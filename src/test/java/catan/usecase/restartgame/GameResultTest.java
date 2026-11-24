package catan.usecase.restartgame;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.Settlement;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class GameResultTest {

    private Game playingGame(int players) {
        List<Player> list = new ArrayList<>();
        for (int i = 1; i <= players; i++) {
            list.add(new Player("P" + i));
        }
        Game game = new Game(BoardFactory.createStandardBoard(new Random(2)), list, WeatherData.none(),
                new Dice(new Random(1)));
        game.startGame();
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex spot = game.getBoard().getVertices().stream()
                    .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true))
                    .findFirst().orElseThrow();
            game.getBoard().placeBuilding(spot, new Settlement(p, spot));
            game.setLastPlacedSettlement(spot);
            game.advanceSetup();
            var road = spot.getAdjacentEdges().get(0);
            game.getBoard().placeRoad(road, new catan.entity.Road(p, road));
            game.advanceSetup();
        }
        return game;
    }

    private static void addCities(Player p, int count) {
        for (int i = 0; i < count; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
    }

    @Test
    void readsTheWinnerAndEveryPlayersScoreInTurnOrder() {
        Game game = playingGame(3);
        addCities(game.getPlayers().get(1), 4); // 2 from setup + 8 = 10
        addCities(game.getPlayers().get(2), 1); // 2 + 2 = 4
        game.getPlayers().get(1).setHasLongestRoad(false);
        game.markRolled();
        game.endTurn(); // P2's turn
        assertTrue(game.checkVictory());

        GameResult result = GameResult.from(game);

        assertEquals("P2", result.winnerName());
        assertEquals(List.of(new PlayerScore("P1", 2), new PlayerScore("P2", 10), new PlayerScore("P3", 4)),
                result.scores());
    }

    @Test
    void scoresIncludeLongestRoadPoints() {
        Game game = playingGame(2);
        Player p1 = game.getPlayers().get(0);
        addCities(p1, 3); // 2 + 6 = 8
        p1.setHasLongestRoad(true); // 10
        assertTrue(game.checkVictory());

        GameResult result = GameResult.from(game);
        assertEquals("P1", result.winnerName());
        assertEquals(new PlayerScore("P1", 10), result.scores().get(0));
        assertEquals(new PlayerScore("P2", 2), result.scores().get(1));
    }

    @Test
    void theWinnersScoreIsAtLeastTen() {
        Game game = playingGame(2);
        addCities(game.getCurrentPlayer(), 6);
        assertTrue(game.checkVictory());
        GameResult result = GameResult.from(game);
        PlayerScore winner = result.scores().stream()
                .filter(s -> s.playerName().equals(result.winnerName())).findFirst().orElseThrow();
        assertTrue(winner.points() >= Game.WINNING_POINTS);
    }

    @Test
    void notAvailableUntilTheGameIsOver() {
        Game inPlay = playingGame(2);
        assertThrows(IllegalStateException.class, () -> GameResult.from(inPlay));

        Game inSetup = new Game(BoardFactory.createStandardBoard(new Random(2)),
                List.of(new Player("A"), new Player("B")), WeatherData.none(), new Dice(new Random(1)));
        assertThrows(IllegalStateException.class, () -> GameResult.from(inSetup));
    }

    @Test
    void scoresAreACopyThatCannotBeChanged() {
        List<PlayerScore> source = new ArrayList<>(List.of(new PlayerScore("A", 10), new PlayerScore("B", 3)));
        GameResult result = new GameResult("A", source);
        source.clear();
        assertEquals(2, result.scores().size());
        assertThrows(UnsupportedOperationException.class, () -> result.scores().add(new PlayerScore("C", 1)));
    }

    @Test
    void rejectsNulls() {
        assertThrows(NullPointerException.class, () -> new GameResult(null, List.of()));
        assertThrows(NullPointerException.class, () -> new GameResult("A", null));
        assertThrows(NullPointerException.class, () -> GameResult.from(null));
    }
}
