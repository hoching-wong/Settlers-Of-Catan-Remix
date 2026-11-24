package catan.framework.persistence;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.WeatherData;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

class InMemoryGameRepositoryTest {
    private final InMemoryGameRepository repository = new InMemoryGameRepository();

    private Game newGame() {
        return new Game(BoardFactory.createStandardBoard(new Random(1)),
                List.of(new Player("Ann"), new Player("Bob")), WeatherData.none(), new Dice(new Random(1)));
    }

    @Test
    void startsWithNoGame() {
        assertNull(repository.get());
    }

    @Test
    void returnsTheSavedGame() {
        Game game = newGame();
        repository.save(game);
        assertSame(game, repository.get());
    }

    @Test
    void savingAgainReplacesTheGame() {
        Game first = newGame();
        Game second = newGame();
        repository.save(first);
        repository.save(second);
        assertSame(second, repository.get());
    }

    @Test
    void clearForgetsTheGame() {
        repository.save(newGame());
        repository.clear();
        assertNull(repository.get());
    }

    @Test
    void clearingWithNoGameIsFine() {
        repository.clear();
        assertNull(repository.get());
    }

    @Test
    void savingNullIsRejectedAndKeepsTheOldGame() {
        Game game = newGame();
        repository.save(game);
        assertThrows(NullPointerException.class, () -> repository.save(null));
        assertSame(game, repository.get());
    }
}
