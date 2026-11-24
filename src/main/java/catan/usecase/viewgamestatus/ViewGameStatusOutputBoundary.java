package catan.usecase.viewgamestatus;

/** Output boundary for displaying the current game status. */
public interface ViewGameStatusOutputBoundary {

  void prepareSuccessView(ViewGameStatusOutputData outputData);

  void prepareFailView(String errorMessage);
}
