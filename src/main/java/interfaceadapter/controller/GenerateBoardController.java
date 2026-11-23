package interfaceadapter.controller;

import usecase.generateboard.GenerateBoardInputBoundary;
import usecase.generateboard.GenerateBoardInputData;

/** Called when a new game starts (or the player asks for a different board). */
public class GenerateBoardController {
    private final GenerateBoardInputBoundary interactor;

    public GenerateBoardController(GenerateBoardInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** A different random board each time. */
    public void execute() {
        interactor.execute(GenerateBoardInputData.random());
    }

    /** The same board every time for the same seed. */
    public void execute(long seed) {
        interactor.execute(new GenerateBoardInputData(seed));
    }
}
