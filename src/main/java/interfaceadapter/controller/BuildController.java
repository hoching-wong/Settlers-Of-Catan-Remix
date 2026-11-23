package interfaceadapter.controller;

import entity.StructureType;
import usecase.build.BuildInputBoundary;
import usecase.build.BuildInputData;

/** Called when the player clicks a corner or edge while building. */
public class BuildController {
    private final BuildInputBoundary interactor;

    public BuildController(BuildInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * @param structure  "ROAD", "SETTLEMENT" or "CITY"
     * @param locationId the clicked vertex id (settlement, city) or edge id (road)
     */
    public void execute(String structure, int locationId) {
        interactor.execute(new BuildInputData(StructureType.valueOf(structure), locationId));
    }
}
