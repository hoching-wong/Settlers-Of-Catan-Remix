package catan.usecase.startgame;

/** Starts a new game: UC3 (players and board) and UC4 (city and weather). */
public interface StartGameInputBoundary {

    void startGame(StartGameInputData inputData);
}
