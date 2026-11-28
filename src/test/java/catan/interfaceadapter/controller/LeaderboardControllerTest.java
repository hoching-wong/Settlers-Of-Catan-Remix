package catan.interfaceadapter.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import catan.usecase.leaderboard.LeaderboardInputBoundary;
import catan.usecase.leaderboard.LeaderboardInputData;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class LeaderboardControllerTest {

    @Test
    void showAsksTheUseCaseForTheLeaderboardOnce() {
        List<LeaderboardInputData> calls = new ArrayList<>();
        LeaderboardInputBoundary fake = calls::add;

        new LeaderboardController(fake).show();

        assertEquals(1, calls.size());
    }
}
