package catan.usecase.startgame;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.CityLocation;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherUnavailableException;
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
    private final WeatherDataAccessInterface weatherDataAccessObject;
    private final StartGameOutputBoundary outputBoundary;
    private final Random random;

    /**
     * Constructs the start game interactor.
     *
     * @param gameRepository the repository storing the current game
     * @param weatherDataAccessObject the object used to retrieve weather data
     * @param outputBoundary the output boundary
     * @param random used for the board layout and dice
     */
    public StartGameInteractor(
            GameRepository gameRepository,
            WeatherDataAccessInterface weatherDataAccessObject,
            StartGameOutputBoundary outputBoundary,
            Random random) {

        this.gameRepository = Objects.requireNonNull(
                gameRepository, "gameRepository");
        this.weatherDataAccessObject = Objects.requireNonNull(
                weatherDataAccessObject, "weatherDataAccessObject");
        this.outputBoundary = Objects.requireNonNull(
                outputBoundary, "outputBoundary");
        this.random = Objects.requireNonNull(random, "random");
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

        CityLocation city = findCity(inputData.cityName());

        if (inputData.cityName() == null || inputData.cityName().isBlank()) {
            outputBoundary.presentError("Please select a city.");
            return;
        }

        if (city == null) {
            outputBoundary.presentError(
                    "Unknown city: " + inputData.cityName().trim() + ".");
            return;
        }

        boolean weatherAvailable = true;
        WeatherData weather;

        try {
            weather = WeatherData.fromWmoCode(
                    weatherDataAccessObject.fetchWeatherCode(city));

        } catch (WeatherUnavailableException exception) {
            weatherAvailable = false;
            weather = WeatherData.none();
        }

        List<Player> players = new ArrayList<>();

        for (String name : names) {
            players.add(new Player(name));
        }

        Board board = BoardFactory.createStandardBoard(random);
        Game game = new Game(board, players, weather, new Dice(random));

        game.startGame();
        gameRepository.save(game);

        outputBoundary.presentGameStarted(new StartGameOutputData(
                List.copyOf(names),
                city.getName(),
                weatherAvailable,
                weather.getCondition(),
                weather.getBonusResource(),
                weather.getMultiplier()));
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

    /**
     * Finds a city from the predefined city list.
     *
     * @param cityName the selected city name
     * @return the matching city, or null if not found
     */
    private static CityLocation findCity(String cityName) {
        if (cityName == null) {
            return null;
        }

        String wanted = cityName.trim();

        for (CityLocation city : CityLocation.defaults()) {
            if (city.getName().equalsIgnoreCase(wanted)) {
                return city;
            }
        }

        return null;
    }
}
