package catan.usecase.startgame;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.CityLocation;
import catan.entity.Dice;
import catan.entity.Game;
import catan.entity.Player;
import catan.entity.WeatherData;
import catan.usecase.port.GameRepository;
import catan.usecase.port.WeatherGateway;
import catan.usecase.port.WeatherUnavailableException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

public class StartGameInteractor implements StartGameInputBoundary {
    private final GameRepository gameRepository;
    private final WeatherGateway weatherGateway;
    private final StartGameOutputBoundary outputBoundary;
    private final Random random;

    /** @param random used for the board layout and the dice; pass a seeded one in tests */
    public StartGameInteractor(GameRepository gameRepository, WeatherGateway weatherGateway,
                               StartGameOutputBoundary outputBoundary, Random random) {
        this.gameRepository = Objects.requireNonNull(gameRepository, "gameRepository");
        this.weatherGateway = Objects.requireNonNull(weatherGateway, "weatherGateway");
        this.outputBoundary = Objects.requireNonNull(outputBoundary, "outputBoundary");
        this.random = Objects.requireNonNull(random, "random");
    }

    @Override
    public void startGame(StartGameInputData inputData) {
        Objects.requireNonNull(inputData, "inputData");

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
            outputBoundary.presentError("Unknown city: " + inputData.cityName().trim() + ".");
            return;
        }

        boolean weatherAvailable = true;
        WeatherData weather;
        try {
            weather = WeatherData.fromWmoCode(weatherGateway.fetchWeatherCode(city));
        } catch (WeatherUnavailableException e) {
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

    /** Fills {@code cleanNames} with the trimmed names. Returns an error message, or null if all is well. */
    private static String validateNames(List<String> names, List<String> cleanNames) {
        if (names.size() < Game.MIN_PLAYERS || names.size() > Game.MAX_PLAYERS) {
            return "Please choose " + Game.MIN_PLAYERS + " to " + Game.MAX_PLAYERS + " players.";
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
                return "Player names must be different: \"" + name + "\" is used twice.";
            }
        }
        return null;
    }

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
