package catan.usecase.port;

import catan.entity.Board;

/**
 * Where the generate-board use case keeps the board it made for the next game, until the
 * game starts. It is separate from {@link GameRepository} so the use cases that only need
 * the game do not have to know about it (Interface Segregation).
 */
public interface BoardRepository {

    /** Stores the board generated for the next game, replacing any earlier one. */
    void saveBoard(Board board);

    /** The board generated for the next game, or null if none has been generated yet. */
    Board getBoard();
}
