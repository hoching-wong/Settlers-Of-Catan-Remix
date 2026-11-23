package catan.framework.persistence;

import catan.entity.Game;
import catan.usecase.port.GameRepository;
import java.util.Objects;

/** Keeps the current game in memory. It is lost when the program closes. */
public class InMemoryGameRepository implements GameRepository {
    private Game game;

    @Override
    public void save(Game game) {
        this.game = Objects.requireNonNull(game, "game");
    }

    @Override
    public Game get() {
        return game;
    }

    @Override
    public void clear() {
        game = null;
    }
}
