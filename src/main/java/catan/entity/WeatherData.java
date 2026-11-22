
package catan.entity;

/**
 * Represents a weather condition and its effect on resource production.
 * Weather data remains unchanged after it is created.
 */
public class WeatherData {

    private static final int SUNNY_MIN = 0;
    private static final int SUNNY_MAX = 1;
    private static final int CLOUDY_MIN = 2;
    private static final int CLOUDY_MAX = 3;
    private static final int FOG_CODE_1 = 45;
    private static final int FOG_CODE_2 = 48;
    private static final int RAINY_MIN_1 = 51;
    private static final int RAINY_MAX_1 = 67;
    private static final int RAINY_MIN_2 = 80;
    private static final int RAINY_MAX_2 = 82;
    private static final int SNOWY_MIN_1 = 71;
    private static final int SNOWY_MAX_1 = 77;
    private static final int SNOWY_MIN_2 = 85;
    private static final int SNOWY_MAX_2 = 86;
    private static final int STORMY_MIN = 95;
    private static final int STORMY_MAX = 99;
    private static final int NORMAL_MULTIPLIER = 1;
    private static final int BONUS_MULTIPLIER = 2;

    private final WeatherType condition;
    private final ResourceType bonusResource;
    private final int multiplier;

    /**
     * Constructs weather data with the given resource bonus.
     *
     * @param condition the weather condition, or null if unclassified
     * @param bonusResource the resource receiving a bonus, or null if none
     * @param multiplier the multiplier applied to the bonus resource
     */
    private WeatherData(WeatherType condition, ResourceType bonusResource, int multiplier) {
        this.condition = condition;
        this.bonusResource = bonusResource;
        this.multiplier = multiplier;
    }

    /**
     * Classifies a WMO weather code using the game's weather bonus rules.
     * Fog has no bonus; unclassified codes result in {@link #none()}.
     *
     * @param code the WMO weather code returned by Open-Meteo
     * @return the corresponding weather data
     */
    public static WeatherData fromWmoCode(int code) {
        if (code >= SUNNY_MIN && code <= SUNNY_MAX) {
            return new WeatherData(WeatherType.SUNNY, ResourceType.LUMBER, BONUS_MULTIPLIER);
        }
        if (code >= CLOUDY_MIN && code <= CLOUDY_MAX) {
            return new WeatherData(WeatherType.CLOUDY, ResourceType.BRICK, BONUS_MULTIPLIER);
        }
        if ((code >= RAINY_MIN_1 && code <= RAINY_MAX_1)
                || (code >= RAINY_MIN_2 && code <= RAINY_MAX_2)) {
            return new WeatherData(WeatherType.RAINY, ResourceType.GRAIN, BONUS_MULTIPLIER);
        }
        if ((code >= SNOWY_MIN_1 && code <= SNOWY_MAX_1)
                || (code >= SNOWY_MIN_2 && code <= SNOWY_MAX_2)) {
            return new WeatherData(WeatherType.SNOWY, ResourceType.WOOL, BONUS_MULTIPLIER);
        }
        if (code >= STORMY_MIN && code <= STORMY_MAX) {
            return new WeatherData(WeatherType.STORMY, ResourceType.ORE, BONUS_MULTIPLIER);
        }
        if (code == FOG_CODE_1 || code == FOG_CODE_2) {
            return new WeatherData(WeatherType.FOG, null, NORMAL_MULTIPLIER);
        }
        return none();
    }

    /**
     * Creates weather data without a classified condition or resource bonus.
     *
     * @return weather data with a multiplier of one and no bonus resource
     */
    public static WeatherData none() {
        return new WeatherData(null, null, NORMAL_MULTIPLIER);
    }

    /**
     * Returns the classified weather condition.
     *
     * @return the condition, or null if the weather is unclassified
     */
    public WeatherType getCondition() {
        return condition;
    }

    /**
     * Returns the resource receiving a weather bonus.
     *
     * @return the bonus resource, or null if no bonus applies
     */
    public ResourceType getBonusResource() {
        return bonusResource;
    }

    /**
     * Returns the resource production multiplier.
     *
     * @return two if the weather provides a bonus; otherwise one
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Checks whether this weather condition provides a resource bonus.
     *
     * @return true if a resource has a bonus; otherwise false
     */
    public boolean hasBonus() {
        return bonusResource != null;
    }
}

