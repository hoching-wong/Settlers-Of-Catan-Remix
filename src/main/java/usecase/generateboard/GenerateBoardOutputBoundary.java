package usecase.generateboard;

/** Implemented by the presenter that shows the new board. */
public interface GenerateBoardOutputBoundary {
    void prepareSuccessView(GenerateBoardOutputData outputData);

    void prepareFailView(String errorMessage);
}
