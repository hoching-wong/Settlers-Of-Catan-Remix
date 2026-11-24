package catan.usecase;

import catan.entity.Board;
import catan.entity.Game;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;

/** Keeps the board and game in fields so tests can set them up and check them directly. */
public class FakeGameRepository implements GameRepository, BoardRepository {
    public Board board;
    public Game game;
    public int gameSaves;

    @Override
    public void saveBoard(Board board) {
        this.board = board;
    }

    @Override
    public Board getBoard() {
        return board;
    }

    @Override
    public void save(Game game) {
        this.game = game;
        gameSaves++;
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
