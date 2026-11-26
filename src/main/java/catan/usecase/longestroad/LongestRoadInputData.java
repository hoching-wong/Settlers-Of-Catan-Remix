package catan.usecase.longestroad;

import catan.entity.Game;

/**
 * Input data for recalculating the Longest Road.
 *
 * @param game the current game
 */
public record LongestRoadInputData(Game game) {}
