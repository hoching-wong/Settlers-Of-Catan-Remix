package catan.usecase.rolldice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The result of a roll.
 *
 * @param playerName who rolled
 * @param dieOne     the first die
 * @param dieTwo     the second die
 * @param total      the two dice added together
 * @param gains      what each player received, by player name and then resource name. Players who
 *                   received nothing are left out.
 */
public record RollDiceOutputData(String playerName, int dieOne, int dieTwo, int total,
                                 Map<String, Map<String, Integer>> gains) {
    public RollDiceOutputData {
        Map<String, Map<String, Integer>> copy = new LinkedHashMap<>();
        gains.forEach((name, resources) -> copy.put(name, Map.copyOf(resources)));
        gains = Map.copyOf(copy);
    }
}
