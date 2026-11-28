package catan.interfaceadapter.controller;

import catan.usecase.leaderboard.LeaderboardInputBoundary;
import catan.usecase.leaderboard.LeaderboardInputData;
import java.util.Objects;

/** Asks for the leaderboard to be loaded. */
public class LeaderboardController {

    private final LeaderboardInputBoundary interactor;

    public LeaderboardController(LeaderboardInputBoundary interactor) {
        this.interactor = Objects.requireNonNull(interactor);
    }

    public void show() {
        interactor.showLeaderboard(new LeaderboardInputData());
    }
}
