package catan.usecase.placeinitial;

/** Implemented by the presenter. */
public interface PlaceInitialOutputBoundary {

    /** The settlement or road was placed. */
    void presentPlacement(PlaceInitialOutputData outputData);

    /** The placement was not allowed. Nothing changed, so the same player tries again. */
    void presentError(String message);
}
