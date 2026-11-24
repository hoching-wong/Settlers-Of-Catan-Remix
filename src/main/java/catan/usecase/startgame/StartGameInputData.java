package catan.usecase.startgame;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the players chose on the start menu.
 *
 * @param playerNames the names in entry order; the first one is Player 1. A null list counts as empty.
 * @param cityName the selected city, or null or blank if none was chosen
 */
public record StartGameInputData(List<String> playerNames, String cityName) {

    public StartGameInputData {
        playerNames = playerNames == null
                ? List.of()
                : Collections.unmodifiableList(new ArrayList<>(playerNames));
    }
}
