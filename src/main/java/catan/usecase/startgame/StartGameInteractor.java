package catan.usecase.startgame;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.WeatherData;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;
import catan.usecase.weather.WeatherInputBoundary;
import catan.usecase.weather.WeatherInputData;
import catan.usecase.weather.WeatherOutputData;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * Handles starting a new game, including retrieving weather data.
 */
public class StartGameInteractor implements StartGameInputBoundary {

    private final GameRepository gameRepository;
    private final WeatherInputBoundary weatherInteractor;
    private final StartGameOutputBoundary outputBoundary;
    private final Random random;
    private final BoardRepository boardRepository;

    /**
     * Without a board repository: every game gets a new random board made here.
     *
     * @param random used for the board layout and the dice; pass a seeded one in tests
     */
    public StartGameInteractor(
            GameRepository gameRepository,
            WeatherInputBoundary weatherInteractor,
            StartGameOutputBoundary outputBoundary,
            Random random) {
        this(gameRepository, weatherInteractor, outputBoundary,
                random, new NoGeneratedBoard());
    }

    /**
     * The game is played on the board the generate-board use case (UC8) stored in
     * {@code boardRepository}, so it is the same board the player sees. If none has been
     * generated, a random board is made here instead.
     *
     * @param random used for the dice, and for the board when none was generated
     */
    public StartGameInteractor(
            GameRepository gameRepository,
            WeatherInputBoundary weatherInteractor,
            StartGameOutputBoundary outputBoundary,
            Random random,
            BoardRepository boardRepository) {

        this.gameRepository = Objects.requireNonNull(
                gameRepository, "gameRepository");
        this.weatherInteractor = Objects.requireNonNull(
                weatherInteractor, "weatherInteractor");
        this.outputBoundary = Objects.requireNonNull(
                outputBoundary, "outputBoundary");
        this.random = Objects.requireNonNull(
                random, "random");
        this.boardRepository = Objects.requireNonNull(
                boardRepository, "boardRepository");
    }

    /**
     * Validates the input and starts a new game.
     *
     * @param inputData the information required to start a game
     */
    @Override
    public void startGame(StartGameInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

        // Never replace a game that is still being played.
        Game current = gameRepository.get();

        if (current != null && current.getPhase() != GamePhase.GAME_OVER) {
            outputBoundary.presentError("A game is already in progress.");
            return;
        }

        List<String> names = new ArrayList<>();
        String nameError = validateNames(inputData.playerNames(), names);

        if (nameError != null) {
            outputBoundary.presentError(nameError);
            return;
        }

        WeatherOutputData weatherOutputData;

        try {
            weatherOutputData = weatherInteractor.execute(
                    new WeatherInputData(inputData.cityName()));
        } catch (IllegalArgumentException exception) {
            outputBoundary.presentError(exception.getMessage());
            return;
        }

        WeatherData weather = weatherOutputData.getWeather();

        List<Player> players = new ArrayList<>();

        for (String name : names) {
            players.add(new Player(name));
        }

        Board board = takeBoard();
        Game game = new Game(board, players, weather, new Dice(random));

        game.startGame();
        gameRepository.save(game);

        outputBoundary.presentGameStarted(new StartGameOutputData(
                List.copyOf(names),
                weatherOutputData.getCity().getName(),
                weatherOutputData.isWeatherAvailable(),
                weather.getCondition(),
                weather.getBonusResource(),
                weather.getMultiplier()));
    }

    /**
     * The generated board, which is then removed from the repository so the next game gets a
     * new one. A random board if none was generated.
     */
    private Board takeBoard() {
        Board generated = boardRepository.getBoard();
        if (generated == null) {
            return BoardFactory.createStandardBoard(random);
        }
        boardRepository.saveBoard(null);
        return generated;
    }

    /** Used when there is no generate-board use case: never has a board. */
    private static final class NoGeneratedBoard implements BoardRepository {
        @Override
        public void saveBoard(Board board) {
            // nothing is kept
        }

        @Override
        public Board getBoard() {
            return null;
        }
    }

    /**
     * Validates player names and stores the trimmed names.
     *
     * @param names the original player names
     * @param cleanNames the list used to store validated names
     * @return an error message, or null if the names are valid
     */
    private static String validateNames(
            List<String> names, List<String> cleanNames) {

        if (names.size() < Game.MIN_PLAYERS
                || names.size() > Game.MAX_PLAYERS) {

            return "Please choose "
                    + Game.MIN_PLAYERS
                    + " to "
                    + Game.MAX_PLAYERS
                    + " players.";
        }

        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);

            if (name == null || name.isBlank()) {
                return "Player " + (i + 1) + " needs a name.";
            }

            cleanNames.add(name.trim());
        }

        Set<String> seen = new HashSet<>();

        for (String name : cleanNames) {
            if (!seen.add(name.toLowerCase(Locale.ROOT))) {
                return "Player names must be different: \""
                        + name
                        + "\" is used twice.";
            }
        }

        return null;
    }
}
