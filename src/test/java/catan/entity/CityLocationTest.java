package catan.entity;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CityLocationTest {
    @Test
    void gettersReturnConstructorValues() {
        CityLocation c = new CityLocation("Paris", 48.85, 2.35);
        assertEquals("Paris", c.getName());
        assertEquals(48.85, c.getLatitude());
        assertEquals(2.35, c.getLongitude());
    }

    @Test
    void defaultsContainReadmeCitiesWithUniqueNames() {
        List<CityLocation> cities = CityLocation.defaults();
        Set<String> names = new HashSet<>();
        for (CityLocation c : cities) {
            names.add(c.getName());
            assertTrue(c.getLatitude() >= -90 && c.getLatitude() <= 90);
            assertTrue(c.getLongitude() >= -180 && c.getLongitude() <= 180);
        }
        assertEquals(cities.size(), names.size());
        assertTrue(names.containsAll(Set.of("Toronto", "London", "Tokyo", "New York")));
    }
}
