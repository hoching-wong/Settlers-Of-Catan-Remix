package catan.usecase.startgame;

import catan.entity.ResourceType;
import catan.entity.WeatherType;
import java.util.List;

/**
 * What the start menu shows once a game has been created (UC4 step 8).
 *
 * @param playerNames the names in turn order, Player 1 first
 * @param cityName the selected city
 * @param weatherAvailable false if the weather request failed
 * @param condition the weather category, or null if there is none (request failed or code uncategorised)
 * @param bonusResource the resource that is doubled, or null if there is no bonus
 * @param multiplier how much the bonus resource is multiplied by; 1 if there is no bonus
 */
public record StartGameOutputData(
        List<String> playerNames,
        String cityName,
        boolean weatherAvailable,
        WeatherType condition,
        ResourceType bonusResource,
        int multiplier) {

    public boolean hasBonus() {
        return bonusResource != null;
    }
}
