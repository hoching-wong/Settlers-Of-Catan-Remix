package usecase.selectstructure;

/** Implemented by the presenter that highlights the valid locations. */
public interface SelectStructureOutputBoundary {
    void prepareSuccessView(SelectStructureOutputData outputData);

    void prepareFailView(String errorMessage);
}
