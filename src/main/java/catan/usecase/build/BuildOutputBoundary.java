package catan.usecase.build;

/** Implemented by the presenter that shows the result of a build. */
public interface BuildOutputBoundary {
    void prepareSuccessView(BuildOutputData outputData);

    void prepareFailView(String errorMessage);
}
