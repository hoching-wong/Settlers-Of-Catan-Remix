package catan.usecase.restartgame;

/**
 * @param choice the button that was pressed, so the view can also close the window for EXIT
 */
public record RestartGameOutputData(RestartGameInputData.Choice choice) {
}
