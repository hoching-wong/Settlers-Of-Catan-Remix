package catan.usecase.port;

import catan.entity.Game;

/** Where the one current game is kept between use case calls. Implemented in the framework layer. */
public interface GameRepository {

    /** Stores the game as the current one, replacing any earlier game. */
    void save(Game game);

    /** The current game, or null if there is none. */
    Game get();

    /** Forgets the current game, for example when restarting. */
    void clear();
}
