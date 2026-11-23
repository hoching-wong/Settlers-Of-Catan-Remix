package usecase.selectstructure;

import entity.StructureType;

/** @param structure the structure the player wants to build next */
public record SelectStructureInputData(StructureType structure) {}
