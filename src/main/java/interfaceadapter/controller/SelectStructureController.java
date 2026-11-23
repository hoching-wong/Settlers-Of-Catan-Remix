package interfaceadapter.controller;

import entity.StructureType;
import usecase.selectstructure.SelectStructureInputBoundary;
import usecase.selectstructure.SelectStructureInputData;

/** Called when the player picks Road, Settlement or City from the build menu. */
public class SelectStructureController {
    private final SelectStructureInputBoundary interactor;

    public SelectStructureController(SelectStructureInputBoundary interactor) {
        this.interactor = interactor;
    }

    /** @param structure "ROAD", "SETTLEMENT" or "CITY" */
    public void execute(String structure) {
        interactor.execute(new SelectStructureInputData(StructureType.valueOf(structure)));
    }
}
