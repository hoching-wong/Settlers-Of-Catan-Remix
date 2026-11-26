package catan.usecase.startgame;

import static org.junit.jupiter.api.Assertions.*;

import catan.entity.Board;
import catan.entity.BoardFactory;
import catan.entity.City;
import catan.entity.CityLocation;
import catan.entity.Dice;
import catan.entity.Edge;
import catan.entity.Game;
import catan.entity.GamePhase;
import catan.entity.Player;
import catan.entity.ResourceType;
import catan.entity.Road;
import catan.entity.Settlement;
import catan.entity.SetupStep;
import catan.entity.Tile;
import catan.entity.Vertex;
import catan.entity.WeatherData;
import catan.entity.WeatherType;
import catan.usecase.port.BoardRepository;
import catan.usecase.port.GameRepository;
import catan.usecase.weather.WeatherDataAccessInterface;
import catan.usecase.weather.WeatherInteractor;
import catan.usecase.weather.WeatherUnavailableException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class StartGameInteractorTest {

    // ---- fakes ----

    private static class FakeGameRepository implements GameRepository {
        Game game;
        int saves;

        @Override
        public void save(Game game) {
            this.game = game;
            saves++;
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

    private static class FakeWeatherDataAccessObject implements WeatherDataAccessInterface {
        int code;
        boolean fail;
        int calls;
        CityLocation lastCity;

        @Override
        public int fetchWeatherCode(CityLocation city) throws WeatherUnavailableException {
            calls++;
            lastCity = city;
            if (fail) {
                throw new WeatherUnavailableException("offline");
            }
            return code;
        }
    }

    private static class RecordingPresenter implements StartGameOutputBoundary {
        final List<StartGameOutputData> started = new ArrayList<>();
        final List<String> errors = new ArrayList<>();

        @Override
        public void presentGameStarted(StartGameOutputData outputData) {
            started.add(outputData);
        }

        @Override
        public void presentError(String message) {
            errors.add(message);
        }
    }

    private static class FakeBoardRepository implements BoardRepository {
        Board board;

        @Override
        public void saveBoard(Board board) {
            this.board = board;
        }

        @Override
        public Board getBoard() {
            return board;
        }
    }

    private final FakeGameRepository repository = new FakeGameRepository();
    private final FakeWeatherDataAccessObject weather = new FakeWeatherDataAccessObject();
    private final WeatherInteractor weatherInteractor = new WeatherInteractor(weather);
    private final RecordingPresenter presenter = new RecordingPresenter();
    private final StartGameInteractor interactor =
            new StartGameInteractor(repository, weatherInteractor, presenter, new Random(1));

    private void start(String city, String... names) {
        interactor.startGame(new StartGameInputData(Arrays.asList(names), city));
    }

    private void assertRejected(String expectedMessage) {
        assertEquals(List.of(expectedMessage), presenter.errors);
        assertTrue(presenter.started.isEmpty());
        assertNull(repository.get(), "no game should be created");
        assertEquals(0, repository.saves);
        assertEquals(0, weather.calls, "no weather request for invalid input");
    }

    // ---- success ----

    @Test
    void startsAGameForTwoThreeAndFourPlayers() {
        for (int n = 2; n <= 4; n++) {
            FakeGameRepository repo = new FakeGameRepository();
            RecordingPresenter out = new RecordingPresenter();
            StartGameInteractor interactor = new StartGameInteractor(repo, weatherInteractor, out, new Random(1));
            List<String> names = List.of("Ann", "Bob", "Cat", "Dan").subList(0, n);

            interactor.startGame(new StartGameInputData(names, "Toronto"));

            assertEquals(1, out.started.size(), n + " players");
            assertTrue(out.errors.isEmpty());
            assertEquals(names, out.started.get(0).playerNames());
            assertEquals(1, repo.saves);
            Game game = repo.get();
            assertEquals(n, game.getPlayers().size());
            for (int i = 0; i < n; i++) {
                assertEquals(names.get(i), game.getPlayers().get(i).getName());
            }
        }
    }

    @Test
    void newGameIsInSetupWithPlayerOneToPlaceASettlement() {
        start("Toronto", "Ann", "Bob");
        Game game = repository.get();
        assertEquals(GamePhase.SETUP, game.getPhase());
        assertEquals(SetupStep.SETTLEMENT, game.getSetupStep());
        assertEquals(0, game.getSetupIndex());
        assertEquals("Ann", game.getCurrentPlayer().getName());
        assertNull(game.getWinner());
    }

    @Test
    void theBoardIsAStandardBoardWithNothingBuilt() {
        start("Toronto", "Ann", "Bob");
        Game game = repository.get();
        assertEquals(19, game.getBoard().getTiles().size());
        assertEquals(54, game.getBoard().getVertices().size());
        assertEquals(72, game.getBoard().getEdges().size());
        game.getBoard().getVertices().forEach(v -> assertFalse(v.isOccupied()));
        for (Player p : game.getPlayers()) {
            assertEquals(0, p.getResources().getTotalResources());
            assertEquals(0, p.getVictoryPoints());
        }
    }

    @Test
    void namesAreTrimmedAndKeepTheirCase() {
        start("Toronto", "  Ann ", "bob");
        assertEquals(List.of("Ann", "bob"), presenter.started.get(0).playerNames());
        assertEquals("Ann", repository.get().getPlayers().get(0).getName());
    }

    @Test
    void cityIsFoundIgnoringCaseAndSpaces() {
        start("  new york ", "Ann", "Bob");
        assertEquals("New York", presenter.started.get(0).cityName());
        assertEquals("New York", weather.lastCity.getName());
    }

    @Test
    void theWeatherDataAccessObjectGetsTheSelectedCitysCoordinates() {
        start("Tokyo", "Ann", "Bob");
        CityLocation tokyo = CityLocation.defaults().stream()
                .filter(c -> c.getName().equals("Tokyo")).findFirst().orElseThrow();
        assertEquals(tokyo.getLatitude(), weather.lastCity.getLatitude());
        assertEquals(tokyo.getLongitude(), weather.lastCity.getLongitude());
        assertEquals(1, weather.calls, "the weather is fetched once");
    }

    @Test
    void everyDefaultCityCanBeSelected() {
        for (CityLocation city : CityLocation.defaults()) {
            presenter.started.clear();
            repository.clear();
            start(city.getName(), "Ann", "Bob");
            assertEquals(city.getName(), presenter.started.get(0).cityName());
        }
    }

    /** A game played to the end, saved as the current game. */
    private Game finishedGameInRepository() {
        Game game = new Game(BoardFactory.createStandardBoard(new Random(2)),
                List.of(new Player("Old1"), new Player("Old2")), WeatherData.none(), new Dice(new Random(2)));
        game.startGame();
        while (game.getPhase() == GamePhase.SETUP) {
            Player p = game.getCurrentPlayer();
            Vertex spot = game.getBoard().getVertices().stream()
                    .filter(v -> game.getBoard().isValidSettlementLocation(v, p, true)).findFirst().orElseThrow();
            game.getBoard().placeBuilding(spot, new Settlement(p, spot));
            game.setLastPlacedSettlement(spot);
            game.advanceSetup();
            Edge road = spot.getAdjacentEdges().get(0);
            game.getBoard().placeRoad(road, new Road(p, road));
            game.advanceSetup();
        }
        Player p = game.getCurrentPlayer();
        for (int i = 0; i < 4; i++) {
            p.addBuilding(new City(p, new Vertex(1000 + i)));
        }
        assertTrue(game.checkVictory());
        repository.save(game);
        repository.saves = 0;
        return game;
    }

    @Test
    void startingWhileAGameIsUnfinishedIsRefused() {
        start("Toronto", "Ann", "Bob");
        Game unfinished = repository.get();
        presenter.errors.clear();

        start("London", "Cat", "Dan", "Eve");

        assertEquals(List.of("A game is already in progress."), presenter.errors);
        assertEquals(1, presenter.started.size(), "only the first start succeeded");
        assertSame(unfinished, repository.get());
        assertEquals(1, repository.saves);
        assertEquals(1, weather.calls, "no weather request for the refused start");
    }

    @Test
    void aFinishedGameCanBeReplaced() {
        Game finished = finishedGameInRepository();

        start("London", "Cat", "Dan", "Eve");

        assertTrue(presenter.errors.isEmpty());
        assertNotSame(finished, repository.get());
        assertEquals(3, repository.get().getPlayers().size());
        assertEquals(1, repository.saves);
    }

    // ---- weather ----

    @ParameterizedTest
    @CsvSource({
            "0, SUNNY, LUMBER", "1, SUNNY, LUMBER",
            "2, CLOUDY, BRICK", "3, CLOUDY, BRICK",
            "61, RAINY, GRAIN", "82, RAINY, GRAIN",
            "71, SNOWY, WOOL", "86, SNOWY, WOOL",
            "95, STORMY, ORE", "99, STORMY, ORE"})
    void weatherCodeGivesTheBonus(int code, WeatherType condition, ResourceType resource) {
        weather.code = code;
        start("Toronto", "Ann", "Bob");

        StartGameOutputData out = presenter.started.get(0);
        assertTrue(out.weatherAvailable());
        assertEquals(condition, out.condition());
        assertEquals(resource, out.bonusResource());
        assertEquals(2, out.multiplier());
        assertTrue(out.hasBonus());

        WeatherData stored = repository.get().getWeather();
        assertEquals(condition, stored.getCondition());
        assertEquals(resource, stored.getBonusResource());
        assertEquals(2, stored.getMultiplier());
    }

    @Test
    void fogIsReportedButGivesNoBonus() {
        weather.code = 45;
        start("Toronto", "Ann", "Bob");
        StartGameOutputData out = presenter.started.get(0);
        assertTrue(out.weatherAvailable());
        assertEquals(WeatherType.FOG, out.condition());
        assertFalse(out.hasBonus());
        assertEquals(1, out.multiplier());
        assertFalse(repository.get().getWeather().hasBonus());
    }

    @Test
    void aCodeWithNoCategoryGivesNormalProduction() {
        weather.code = 1000;
        start("Toronto", "Ann", "Bob");
        StartGameOutputData out = presenter.started.get(0);
        assertTrue(out.weatherAvailable());
        assertNull(out.condition());
        assertFalse(out.hasBonus());
        assertFalse(repository.get().getWeather().hasBonus());
    }

    @Test
    void aFailedWeatherRequestStillStartsTheGameWithoutABonus() {
        weather.fail = true;
        start("Toronto", "Ann", "Bob");

        assertTrue(presenter.errors.isEmpty());
        StartGameOutputData out = presenter.started.get(0);
        assertFalse(out.weatherAvailable());
        assertNull(out.condition());
        assertNull(out.bonusResource());
        assertFalse(out.hasBonus());
        assertEquals(1, out.multiplier());

        Game game = repository.get();
        assertNotNull(game);
        assertFalse(game.getWeather().hasBonus());
        assertEquals(GamePhase.SETUP, game.getPhase());
    }

    // ---- validation ----

    @Test
    void tooFewPlayersAreRejected() {
        start("Toronto", "Ann");
        assertRejected("Please choose 2 to 4 players.");
    }

    @Test
    void noPlayersAreRejected() {
        interactor.startGame(new StartGameInputData(null, "Toronto"));
        assertRejected("Please choose 2 to 4 players.");
    }

    @Test
    void tooManyPlayersAreRejected() {
        start("Toronto", "A", "B", "C", "D", "E");
        assertRejected("Please choose 2 to 4 players.");
    }

    @Test
    void anEmptyNameIsRejectedWithItsPlayerNumber() {
        start("Toronto", "Ann", "");
        assertRejected("Player 2 needs a name.");
    }

    @Test
    void aBlankOrNullNameIsRejected() {
        start("Toronto", "   ", "Bob");
        assertEquals(List.of("Player 1 needs a name."), presenter.errors);
        presenter.errors.clear();
        start("Toronto", "Ann", "Bob", null);
        assertEquals(List.of("Player 3 needs a name."), presenter.errors, "a null entry counts as no name");
    }

    @Test
    void duplicateNamesAreRejected() {
        start("Toronto", "Ann", "Bob", "Ann");
        assertRejected("Player names must be different: \"Ann\" is used twice.");
    }

    @Test
    void duplicatesIgnoreCaseAndSpaces() {
        start("Toronto", "Ann", " ann ");
        assertEquals(1, presenter.errors.size());
        assertTrue(presenter.errors.get(0).startsWith("Player names must be different"));
        assertNull(repository.get());
    }

    @Test
    void aMissingCityIsRejected() {
        start(null, "Ann", "Bob");
        assertRejected("Please select a city.");
    }

    @Test
    void aBlankCityIsRejected() {
        start("   ", "Ann", "Bob");
        assertRejected("Please select a city.");
    }

    @Test
    void anUnknownCityIsRejected() {
        start("Atlantis", "Ann", "Bob");
        assertRejected("Unknown city: Atlantis.");
    }

    @Test
    void theFirstProblemIsTheOneReported() {
        start(null, "Ann", "Ann"); // bad names and no city
        assertEquals(1, presenter.errors.size());
        assertTrue(presenter.errors.get(0).startsWith("Player names"));
    }

    @Test
    void invalidInputKeepsTheFinishedGame() {
        Game finished = finishedGameInRepository();
        start("Toronto", "Ann");
        assertEquals(List.of("Please choose 2 to 4 players."), presenter.errors);
        assertSame(finished, repository.get());
        assertEquals(0, repository.saves);
        assertEquals(0, weather.calls);
    }

    // ---- determinism and construction ----

    private static String layout(Game game) {
        StringBuilder sb = new StringBuilder();
        for (Tile t : game.getBoard().getTiles()) {
            sb.append(t.getTerrain()).append(':').append(t.getNumberToken()).append(' ');
        }
        return sb.toString();
    }

    @Test
    void theSameSeedGivesTheSameBoardAndDice() {
        FakeGameRepository repoA = new FakeGameRepository();
        FakeGameRepository repoB = new FakeGameRepository();
        new StartGameInteractor(repoA, weatherInteractor, new RecordingPresenter(), new Random(5))
                .startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));
        new StartGameInteractor(repoB, weatherInteractor, new RecordingPresenter(), new Random(5))
                .startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));
        assertEquals(layout(repoA.get()), layout(repoB.get()));

        repoA.get().getDice().roll();
        repoB.get().getDice().roll();
        assertEquals(repoA.get().getDice().getTotal(), repoB.get().getDice().getTotal());
    }

    @Test
    void differentSeedsGiveDifferentBoards() {
        List<String> layouts = new ArrayList<>();
        for (long seed = 0; seed < 5; seed++) {
            FakeGameRepository repo = new FakeGameRepository();
            new StartGameInteractor(repo, weatherInteractor, new RecordingPresenter(), new Random(seed))
                    .startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));
            layouts.add(layout(repo.get()));
        }
        assertTrue(layouts.stream().distinct().count() > 1);
    }

    @Test
    void theBoardMatchesTheOneBoardFactoryMakesForTheSameSeed() {
        FakeGameRepository repo = new FakeGameRepository();
        new StartGameInteractor(repo, weatherInteractor, new RecordingPresenter(), new Random(9))
                .startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));
        Game expected = new Game(BoardFactory.createStandardBoard(new Random(9)),
                List.of(new Player("x"), new Player("y")), WeatherData.none(), new Dice(new Random(9)));
        assertEquals(layout(expected), layout(repo.get()));
    }

    // ---- the generated board (UC8) ----

    @Test
    void theGameIsPlayedOnTheGeneratedBoard() {
        FakeBoardRepository boards = new FakeBoardRepository();
        Board generated = BoardFactory.createStandardBoard(new Random(3));
        boards.saveBoard(generated);
        StartGameInteractor withBoards =
                new StartGameInteractor(repository, weatherInteractor, presenter, new Random(1), boards);

        withBoards.startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));

        assertSame(generated, repository.get().getBoard());
        assertNull(boards.getBoard(), "a board is used for one game only");
    }

    @Test
    void withoutAGeneratedBoardARandomOneIsMade() {
        FakeBoardRepository boards = new FakeBoardRepository();
        new StartGameInteractor(repository, weatherInteractor, presenter, new Random(9), boards)
                .startGame(new StartGameInputData(List.of("Ann", "Bob"), "Toronto"));
        Game expected = new Game(BoardFactory.createStandardBoard(new Random(9)),
                List.of(new Player("x"), new Player("y")), WeatherData.none(), new Dice(new Random(9)));
        assertEquals(layout(expected), layout(repository.get()));
    }

    @Test
    void aRejectedStartKeepsTheGeneratedBoard() {
        FakeBoardRepository boards = new FakeBoardRepository();
        Board generated = BoardFactory.createStandardBoard(new Random(3));
        boards.saveBoard(generated);
        new StartGameInteractor(repository, weatherInteractor, presenter, new Random(1), boards)
                .startGame(new StartGameInputData(List.of("Ann", "Ann"), "Toronto"));
        assertEquals(1, presenter.errors.size());
        assertSame(generated, boards.getBoard());
    }

    @Test
    void rejectsANullBoardRepository() {
        assertThrows(NullPointerException.class,
                () -> new StartGameInteractor(repository, weatherInteractor, presenter, new Random(), null));
    }

    @Test
    void inputDataCopiesTheNameList() {
        List<String> names = new ArrayList<>(List.of("Ann", "Bob"));
        StartGameInputData input = new StartGameInputData(names, "Toronto");
        names.add("Cat");
        assertEquals(List.of("Ann", "Bob"), input.playerNames());
        assertThrows(UnsupportedOperationException.class, () -> input.playerNames().add("Dan"));
    }

    @Test
    void constructorAndInputRejectNulls() {
        assertThrows(NullPointerException.class,
                () -> new StartGameInteractor(null, weatherInteractor, presenter, new Random()));
        assertThrows(NullPointerException.class,
                () -> new StartGameInteractor(repository, null, presenter, new Random()));
        assertThrows(NullPointerException.class,
                () -> new StartGameInteractor(repository, weatherInteractor, null, new Random()));
        assertThrows(NullPointerException.class,
                () -> new StartGameInteractor(repository, weatherInteractor, presenter, null));
        assertThrows(NullPointerException.class, () -> interactor.startGame(null));
    }
}
