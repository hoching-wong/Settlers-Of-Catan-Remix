package catan.usecase.restartgame;

import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The outcome of a finished game, shown on the victory screen (UC7). Shared by any use case
 * that can end the game, for example build, and by restart.
 *
 * @param winnerName the winning player
 * @param scores every player's final points, in turn order
 */
public record GameResult(String winnerName, List<PlayerScore> scores) {

    public GameResult {
        Objects.requireNonNull(winnerName, "winnerName");
        scores = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(scores, "scores")));
    }

    /** Reads the result from a finished game. Throws if the game is not over. */
    public static GameResult from(Game game) {
        Objects.requireNonNull(game, "game");
        if (game.getPhase() != GamePhase.GAME_OVER || game.getWinner() == null) {
            throw new IllegalStateException("The game is not over");
        }
        List<PlayerScore> scores = new ArrayList<>();
        for (Player p : game.getPlayers()) {
            scores.add(new PlayerScore(p.getName(), p.getVictoryPoints()));
        }
        return new GameResult(game.getWinner().getName(), scores);
    }
}
