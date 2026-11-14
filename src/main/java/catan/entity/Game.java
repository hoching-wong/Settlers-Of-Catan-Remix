package catan.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The state of one game: whose turn it is, the setup phase in snake order, the dice and
 * the winner. Moves on the board go through {@link Board}; this class tracks the flow.
 */
public class Game {
    public static final int MIN_PLAYERS = 2;
    public static final int MAX_PLAYERS = 4;
    public static final int WINNING_POINTS = 10;

    private final Board board;
    private final List<Player> players;
    private final WeatherData weather;
    private final Dice dice;

    private GamePhase phase = GamePhase.SETUP;
    private int currentPlayerIndex;
    private boolean hasRolled;
    private SetupStep setupStep = SetupStep.SETTLEMENT;
    private int setupIndex;
    private Vertex lastPlacedSettlement;
    private Player longestRoadHolder;
    private Player winner;

    /** Creates a game in the setup phase, with Player 1 about to place the first settlement. */
    public Game(Board board, List<Player> players, WeatherData weather, Dice dice) {
        this.board = Objects.requireNonNull(board, "board");
        this.weather = Objects.requireNonNull(weather, "weather");
        this.dice = Objects.requireNonNull(dice, "dice");
        Objects.requireNonNull(players, "players");
        if (players.size() < MIN_PLAYERS || players.size() > MAX_PLAYERS) {
            throw new IllegalArgumentException(
                    "A game needs " + MIN_PLAYERS + " to " + MAX_PLAYERS + " players, got " + players.size());
        }
        Set<Player> seen = new HashSet<>();
        for (Player p : players) {
            if (p == null) {
                throw new IllegalArgumentException("A player must not be null");
            }
            if (!seen.add(p)) {
                throw new IllegalArgumentException("The same player is listed twice: " + p.getName());
            }
        }
        this.players = List.copyOf(players);
    }

    // ---- setup phase ----

    /** Begins the game by entering the setup phase. Same as {@link #startSetupPhase()}. */
    public void startGame() {
        startSetupPhase();
    }

    /**
     * Puts the game at the very start of setup: Player 1 places a settlement. Throws if the
     * setup has already begun or the game is past setup.
     */
    public void startSetupPhase() {
        if (phase != GamePhase.SETUP || setupIndex != 0 || setupStep != SetupStep.SETTLEMENT
                || lastPlacedSettlement != null) {
            throw new IllegalStateException("The game has already started");
        }
        currentPlayerIndex = setupPlayerIndex(0);
        hasRolled = false;
    }

    /** The player taking each placement turn, in snake order, for example P1 P2 P2 P1. */
    public List<Player> getSetupOrder() {
        List<Player> order = new ArrayList<>();
        for (int i = 0; i < setupTurns(); i++) {
            order.add(players.get(setupPlayerIndex(i)));
        }
        return Collections.unmodifiableList(order);
    }

    private int setupTurns() {
        return players.size() * 2;
    }

    private int setupPlayerIndex(int turn) {
        int n = players.size();
        return turn < n ? turn : 2 * n - 1 - turn;
    }

    /**
     * Records the settlement the current player has just placed during setup, so the road that
     * follows can be checked against it. The vertex must hold the current player's building.
     */
    public void setLastPlacedSettlement(Vertex vertex) {
        requirePhase(GamePhase.SETUP);
        if (setupStep != SetupStep.SETTLEMENT) {
            throw new IllegalStateException("A settlement is not expected at this step");
        }
        Objects.requireNonNull(vertex, "vertex");
        if (!vertex.isOccupied() || vertex.getBuilding().getOwner() != getCurrentPlayer()) {
            throw new IllegalArgumentException("The vertex does not hold the current player's building");
        }
        lastPlacedSettlement = vertex;
    }

    /**
     * Moves setup on after the current player has placed a settlement (SETTLEMENT becomes ROAD)
     * or a road (the next placement turn starts, or regular play begins with Player 1 after the
     * last one).
     */
    public void advanceSetup() {
        requirePhase(GamePhase.SETUP);
        if (setupStep == SetupStep.SETTLEMENT) {
            if (lastPlacedSettlement == null) {
                throw new IllegalStateException("No settlement has been placed this turn");
            }
            setupStep = SetupStep.ROAD;
            return;
        }
        lastPlacedSettlement = null;
        setupIndex++;
        if (setupIndex == setupTurns()) {
            phase = GamePhase.PLAYING;
            setupStep = null;
            currentPlayerIndex = 0;
            hasRolled = false;
        } else {
            setupStep = SetupStep.SETTLEMENT;
            currentPlayerIndex = setupPlayerIndex(setupIndex);
        }
    }

    // ---- playing phase ----

    /** Whether the current player may roll: regular play, and they have not rolled this turn. */
    public boolean canRoll() {
        return phase == GamePhase.PLAYING && !hasRolled;
    }

    /** Records that the current player has rolled this turn. */
    public void markRolled() {
        if (!canRoll()) {
            throw new IllegalStateException("The current player cannot roll now");
        }
        hasRolled = true;
    }

    /** Passes the turn to the next player. The current player must have rolled. */
    public void endTurn() {
        requirePhase(GamePhase.PLAYING);
        if (!hasRolled) {
            throw new IllegalStateException("The current player must roll before ending the turn");
        }
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        hasRolled = false;
    }

    // ---- scoring ----

    /**
     * Ends the game if a player has 10 or more points and returns whether the game is over.
     * The current player is checked first, then the others in turn order. Does nothing during
     * setup.
     */
    public boolean checkVictory() {
        if (phase == GamePhase.GAME_OVER) {
            return true;
        }
        if (phase != GamePhase.PLAYING) {
            return false;
        }
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get((currentPlayerIndex + i) % players.size());
            if (p.getVictoryPoints() >= WINNING_POINTS) {
                winner = p;
                phase = GamePhase.GAME_OVER;
                return true;
            }
        }
        return false;
    }

    /** The winner once the game is over, otherwise null. */
    public Player getWinner() {
        return winner;
    }

    /**
     * Recalculates Longest Road for every player and moves the bonus if the holder changed,
     * keeping each player's {@code hasLongestRoad} flag in step. Call it after anything that
     * can change a road length: a new road, or a settlement or city that breaks a road.
     *
     * @return the holder after the update, or null if nobody qualifies
     */
    public Player updateLongestRoadHolder() {
        Player newHolder = LongestRoadCalculator.findHolder(players, board, longestRoadHolder);
        if (newHolder != longestRoadHolder) {
            if (longestRoadHolder != null) {
                longestRoadHolder.setHasLongestRoad(false);
            }
            if (newHolder != null) {
                newHolder.setHasLongestRoad(true);
            }
            longestRoadHolder = newHolder;
        }
        return longestRoadHolder;
    }

    // ---- accessors ----

    private void requirePhase(GamePhase expected) {
        if (phase != expected) {
            throw new IllegalStateException("Not allowed in phase " + phase + " (needs " + expected + ")");
        }
    }

    public Board getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    /** The player whose turn it is, including the placement turns of setup. */
    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public WeatherData getWeather() {
        return weather;
    }

    public Dice getDice() {
        return dice;
    }

    public boolean hasRolled() {
        return hasRolled;
    }

    /** SETTLEMENT or ROAD during setup, null once setup is over. */
    public SetupStep getSetupStep() {
        return setupStep;
    }

    /** How many placement turns have been completed (0 to twice the number of players). */
    public int getSetupIndex() {
        return setupIndex;
    }

    public Vertex getLastPlacedSettlement() {
        return lastPlacedSettlement;
    }

    public Player getLongestRoadHolder() {
        return longestRoadHolder;
    }
}
