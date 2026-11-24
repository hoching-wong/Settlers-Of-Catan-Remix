package catan.usecase.placeinitial;

/** Places the current player's initial settlement or road during setup, in snake order. */
public interface PlaceInitialInputBoundary {

    void placeInitial(PlaceInitialInputData inputData);
}
