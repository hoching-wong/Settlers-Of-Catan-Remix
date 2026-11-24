package catan.framework.persistence;

import catan.entity.Board;
import catan.usecase.port.BoardRepository;

/** Keeps the board for the next game in memory. It is lost when the program closes. */
public class InMemoryBoardRepository implements BoardRepository {
    private Board board;

    @Override
    public void saveBoard(Board board) {
        this.board = board;
    }

    @Override
    public Board getBoard() {
        return board;
    }
}
