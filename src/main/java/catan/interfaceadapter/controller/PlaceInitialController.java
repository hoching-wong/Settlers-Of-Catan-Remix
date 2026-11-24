package catan.interfaceadapter.controller;

import catan.usecase.placeinitial.PlaceInitialInputBoundary;
import catan.usecase.placeinitial.PlaceInitialInputData;
import java.util.Objects;

public class PlaceInitialController {
    private final PlaceInitialInputBoundary placeInitial;

    public PlaceInitialController(PlaceInitialInputBoundary placeInitial) {
        this.placeInitial = Objects.requireNonNull(placeInitial, "placeInitial");
    }

    /** Called when the player clicks a vertex (settlement) or an edge (road) during setup. */
    public void place(int locationId) {
        placeInitial.placeInitial(new PlaceInitialInputData(locationId));
    }
}
