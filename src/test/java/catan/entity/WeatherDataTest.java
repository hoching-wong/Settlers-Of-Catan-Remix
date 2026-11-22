
package catan.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Tests the weather conditions and resource bonuses in WeatherData.
 */
class WeatherDataTest {

    /**
     * Checks that a weather code produces the expected resource bonus.
     *
     * @param code the WMO weather code
     * @param expectedType the expected weather condition
     * @param expectedResource the expected bonus resource
     */
    private void assertBonus(
            int code, WeatherType expectedType, ResourceType expectedResource) {

        WeatherData weather = WeatherData.fromWmoCode(code);

        assertEquals(expectedType, weather.getCondition());
        assertEquals(expectedResource, weather.getBonusResource());
        assertEquals(2, weather.getMultiplier());
        assertTrue(weather.hasBonus());
    }

    /**
     * Checks that a weather code does not produce a resource bonus.
     *
     * @param code the WMO weather code
     */
    private void assertNoBonus(int code) {
        WeatherData weather = WeatherData.fromWmoCode(code);

        assertFalse(weather.hasBonus());
        assertNull(weather.getBonusResource());
        assertEquals(1, weather.getMultiplier());
    }

    @Test
    void testSunnyWeather() {
        for (int code = 0; code <= 1; code++) {
            assertBonus(code, WeatherType.SUNNY, ResourceType.LUMBER);
        }
    }

    @Test
    void testCloudyWeather() {
        for (int code = 2; code <= 3; code++) {
            assertBonus(code, WeatherType.CLOUDY, ResourceType.BRICK);
        }
    }

    @Test
    void testRainyWeather() {
        for (int code = 51; code <= 67; code++) {
            assertBonus(code, WeatherType.RAINY, ResourceType.GRAIN);
        }

        for (int code = 80; code <= 82; code++) {
            assertBonus(code, WeatherType.RAINY, ResourceType.GRAIN);
        }
    }

    @Test
    void testSnowyWeather() {
        for (int code = 71; code <= 77; code++) {
            assertBonus(code, WeatherType.SNOWY, ResourceType.WOOL);
        }

        for (int code = 85; code <= 86; code++) {
            assertBonus(code, WeatherType.SNOWY, ResourceType.WOOL);
        }
    }

    @Test
    void testStormyWeather() {
        for (int code = 95; code <= 99; code++) {
            assertBonus(code, WeatherType.STORMY, ResourceType.ORE);
        }
    }

    @Test
    void testFogWeather() {
        WeatherData weather = WeatherData.fromWmoCode(45);

        assertEquals(WeatherType.FOG, weather.getCondition());
        assertNoBonus(45);
        assertNoBonus(48);
    }

    @Test
    void testUnknownWeatherCode() {
        int[] unknownCodes = {-1, 4, 50, 68, 70, 100};

        for (int code : unknownCodes) {
            WeatherData weather = WeatherData.fromWmoCode(code);

            assertNull(weather.getCondition());
            assertNoBonus(code);
        }
    }

    @Test
    void testNoWeatherData() {
        WeatherData weather = WeatherData.none();

        assertNull(weather.getCondition());
        assertNull(weather.getBonusResource());
        assertEquals(1, weather.getMultiplier());
        assertFalse(weather.hasBonus());
    }
}

