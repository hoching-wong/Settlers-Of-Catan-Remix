package catan.usecase.generateboard;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.BoardLayoutStrategy;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.usecase.boardstate.BoardSnapshot;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;
import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Generates a random board for the next game and stores it in the {@link BoardRepository}.
 * It refuses while a {@link Game} is still being played.
 *
 * <p>The layout rule is a {@link BoardLayoutStrategy} given to the constructor (Strategy
 * pattern), so the interactor does not change when the team picks a different rule.
 */
public class GenerateBoardInteractor implements GenerateBoardInputBoundary {
    private final GameRepository gameRepository;
    private final BoardRepository boardRepository;
    private final GenerateBoardOutputBoundary presenter;
    private final BoardLayoutStrategy layoutStrategy;

    public GenerateBoardInteractor(GameRepository gameRepository, BoardRepository boardRepository,
                                   GenerateBoardOutputBoundary presenter, BoardLayoutStrategy layoutStrategy) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.boardRepository = Objects.requireNonNull(boardRepository, "boardRepository");
        this.presenter = Objects.requireNonNull(presenter, "presenter");
        this.layoutStrategy = Objects.requireNonNull(layoutStrategy, "layoutStrategy");
    }

    @Override
    public void execute(GenerateBoardInputData inputData) {
        Game current = gameRepository.get();
        if (current != null && current.getPhase() != GamePhase.GAME_OVER) {
            presenter.prepareFailView("A game is already in progress, so the board cannot be changed.");
            return;
        }
        Random random = inputData.seed() == null ? new Random() : new Random(inputData.seed());
        Board board = BoardFactory.createBoard(layoutStrategy, random);
        boardRepository.saveBoard(board);
        presenter.prepareSuccessView(new GenerateBoardOutputData(BoardSnapshot.of(board, List.of())));
    }
}
