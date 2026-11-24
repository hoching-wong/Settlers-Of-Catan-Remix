package catan.usecase.build;

import catan.entity.StructureType;

/**
 * @param structure  what to build
 * @param locationId the vertex id (settlement, city) or edge id (road)
 */
public record BuildInputData(StructureType structure, int locationId) {}
