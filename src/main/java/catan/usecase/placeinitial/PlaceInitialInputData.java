package catan.usecase.placeinitial;

/**
 * Where the current player clicked during setup.
 *
 * @param locationId a vertex id when a settlement is expected, an edge id when a road is expected
 */
public record PlaceInitialInputData(int locationId) {
}
