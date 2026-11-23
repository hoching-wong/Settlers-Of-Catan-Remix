package catan.usecase.restartgame;

/**
 * Leaving the victory screen needs no input: it always clears the finished game and returns to
 * the start menu, where a new game can be set up.
 */
public record RestartGameInputData() {
}
