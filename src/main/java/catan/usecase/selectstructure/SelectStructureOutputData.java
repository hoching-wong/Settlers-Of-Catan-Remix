package catan.usecase.selectstructure;

import java.util.List;

/**
 * @param structure        "ROAD", "SETTLEMENT" or "CITY"
 * @param locationKind     "VERTEX" or "EDGE"
 * @param validLocationIds every vertex or edge id the player can build on now (may be empty)
 * @param playerName       the player who is building
 */
public record SelectStructureOutputData(String structure, String locationKind,
                                        List<Integer> validLocationIds, String playerName) {
    public SelectStructureOutputData {
        validLocationIds = List.copyOf(validLocationIds);
    }
}
