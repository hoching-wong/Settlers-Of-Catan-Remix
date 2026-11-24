package catan.framework.persistence;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.BoardFactory;
import java.util.Random;
import org.junit.jupiter.api.Test;

class InMemoryBoardRepositoryTest {
    private final InMemoryBoardRepository repository = new InMemoryBoardRepository();

    @Test
    void startsWithNoBoard() {
        assertNull(repository.getBoard());
    }

    @Test
    void returnsTheSavedBoard() {
        Board board = BoardFactory.createStandardBoard(new Random(1));
        repository.saveBoard(board);
        assertSame(board, repository.getBoard());
    }

    @Test
    void aNewBoardReplacesTheOldOne() {
        repository.saveBoard(BoardFactory.createStandardBoard(new Random(1)));
        Board second = BoardFactory.createStandardBoard(new Random(2));
        repository.saveBoard(second);
        assertSame(second, repository.getBoard());
    }
}
