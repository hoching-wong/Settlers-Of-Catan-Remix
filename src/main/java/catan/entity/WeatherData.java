package catan.entity;

public class WeatherData {
    private final WeatherType condition;
    private final ResourceType bonusResource;
    private final int multiplier;

    private WeatherData(WeatherType condition, ResourceType bonusResource, int multiplier) {
        this.condition = condition;
        this.bonusResource = bonusResource;
        this.multiplier = multiplier;
    }

    /**
     * Classifies a WMO weather code. Fog (45, 48) has a condition but no bonus.
     * A code with no category gives {@link #none()}.
     */
    public static WeatherData fromWmoCode(int code) {
        if (code >= 0 && code <= 1) {
            return new WeatherData(WeatherType.SUNNY, ResourceType.LUMBER, 2);
        }
        if (code >= 2 && code <= 3) {
            return new WeatherData(WeatherType.CLOUDY, ResourceType.BRICK, 2);
        }
        if ((code >= 51 && code <= 67) || (code >= 80 && code <= 82)) {
            return new WeatherData(WeatherType.RAINY, ResourceType.GRAIN, 2);
        }
        if ((code >= 71 && code <= 77) || (code >= 85 && code <= 86)) {
            return new WeatherData(WeatherType.SNOWY, ResourceType.WOOL, 2);
        }
        if (code >= 95 && code <= 99) {
            return new WeatherData(WeatherType.STORMY, ResourceType.ORE, 2);
        }
        if (code == 45 || code == 48) {
            return new WeatherData(WeatherType.FOG, null, 1);
        }
        return none();
    }

    /** No bonus: the condition is null, the bonus resource is null and the multiplier is 1. */
    public static WeatherData none() {
        return new WeatherData(null, null, 1);
    }

    public WeatherType getCondition() {
        return condition;
    }

    public ResourceType getBonusResource() {
        return bonusResource;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public boolean hasBonus() {
        return bonusResource != null;
    }
}
