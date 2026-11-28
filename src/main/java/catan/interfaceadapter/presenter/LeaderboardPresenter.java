package catan.interfaceadapter.presenter;

import catan.interfaceadapter.viewmodel.LeaderboardRow;
import catan.interfaceadapter.viewmodel.LeaderboardViewModel;
import catan.usecase.leaderboard.LeaderboardOutputBoundary;
import catan.usecase.leaderboard.LeaderboardOutputData;
import java.util.Objects;

/** Puts the leaderboard into its view model. The detail of a failure goes to the log only. */
public class LeaderboardPresenter implements LeaderboardOutputBoundary {

    private final LeaderboardViewModel viewModel;
    private final System.Logger logger;

    public LeaderboardPresenter(LeaderboardViewModel viewModel, System.Logger logger) {
        this.viewModel = Objects.requireNonNull(viewModel);
        this.logger = Objects.requireNonNull(logger);
    }

    @Override
    public void presentLeaderboard(LeaderboardOutputData outputData) {
        viewModel.showRows(outputData.rows().stream()
                .map(row -> new LeaderboardRow(row.rank(), row.name(), row.wins()))
                .toList());
    }

    @Override
    public void presentError(String userMessage, String detail) {
        logger.log(System.Logger.Level.WARNING, "Could not load the leaderboard: " + detail);
        viewModel.showError(userMessage);
    }
}
