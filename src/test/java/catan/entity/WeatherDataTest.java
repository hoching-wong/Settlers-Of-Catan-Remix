package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class WeatherDataTest {
    private static void assertBonus(int code, WeatherType type, ResourceType resource) {
        WeatherData w = WeatherData.fromWmoCode(code);
        assertEquals(type, w.getCondition(), "code " + code);
        assertEquals(resource, w.getBonusResource(), "code " + code);
        assertEquals(2, w.getMultiplier(), "code " + code);
        assertTrue(w.hasBonus(), "code " + code);
    }

    private static void assertNoBonus(int code) {
        WeatherData w = WeatherData.fromWmoCode(code);
        assertFalse(w.hasBonus(), "code " + code);
        assertNull(w.getBonusResource(), "code " + code);
        assertEquals(1, w.getMultiplier(), "code " + code);
    }

    @Test
    void sunny() {
        for (int c = 0; c <= 1; c++) assertBonus(c, WeatherType.SUNNY, ResourceType.LUMBER);
    }

    @Test
    void cloudy() {
        for (int c = 2; c <= 3; c++) assertBonus(c, WeatherType.CLOUDY, ResourceType.BRICK);
    }

    @Test
    void rainy() {
        for (int c = 51; c <= 67; c++) assertBonus(c, WeatherType.RAINY, ResourceType.GRAIN);
        for (int c = 80; c <= 82; c++) assertBonus(c, WeatherType.RAINY, ResourceType.GRAIN);
    }

    @Test
    void snowy() {
        for (int c = 71; c <= 77; c++) assertBonus(c, WeatherType.SNOWY, ResourceType.WOOL);
        for (int c = 85; c <= 86; c++) assertBonus(c, WeatherType.SNOWY, ResourceType.WOOL);
    }

    @Test
    void stormy() {
        for (int c = 95; c <= 99; c++) assertBonus(c, WeatherType.STORMY, ResourceType.ORE);
    }

    @Test
    void fogHasConditionButNoBonus() {
        assertEquals(WeatherType.FOG, WeatherData.fromWmoCode(45).getCondition());
        assertEquals(WeatherType.FOG, WeatherData.fromWmoCode(48).getCondition());
        assertNoBonus(45);
        assertNoBonus(48);
    }

    @Test
    void uncategorisedCodesGiveNoBonus() {
        for (int c : new int[] {-1, 4, 44, 46, 49, 50, 68, 70, 78, 79, 83, 84, 87, 94, 100, 1000}) {
            assertNoBonus(c);
            assertNull(WeatherData.fromWmoCode(c).getCondition(), "code " + c);
        }
    }

    @Test
    void noneHasNoBonus() {
        WeatherData w = WeatherData.none();
        assertFalse(w.hasBonus());
        assertNull(w.getBonusResource());
        assertNull(w.getCondition());
        assertEquals(1, w.getMultiplier());
    }
}
