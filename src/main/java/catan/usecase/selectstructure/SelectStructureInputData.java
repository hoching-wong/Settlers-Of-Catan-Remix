package catan.usecase.selectstructure;

import catan.entity.StructureType;

/** @param structure the structure the player wants to build next */
public record SelectStructureInputData(StructureType structure) {}
