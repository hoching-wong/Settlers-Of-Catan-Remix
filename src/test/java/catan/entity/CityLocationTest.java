package catan.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the CityLocation entity. */
class CityLocationTest {

  @Test
  void testValidCityLocation() {
    CityLocation city = new CityLocation("Toronto", 43.6532, -79.3832);

    assertEquals("Toronto", city.getName());
    assertEquals(43.6532, city.getLatitude());
    assertEquals(-79.3832, city.getLongitude());
  }

  @Test
  void testNullCityName() {
    assertThrows(IllegalArgumentException.class, () -> new CityLocation(null, 43.6532, -79.3832));
  }

  @Test
  void testBlankCityName() {
    assertThrows(IllegalArgumentException.class, () -> new CityLocation("   ", 43.6532, -79.3832));
  }

  @Test
  void testInvalidLatitude() {
    assertThrows(IllegalArgumentException.class, () -> new CityLocation("Toronto", 91.0, -79.3832));

    assertThrows(
        IllegalArgumentException.class, () -> new CityLocation("Toronto", -91.0, -79.3832));
  }

  @Test
  void testInvalidLongitude() {
    assertThrows(IllegalArgumentException.class, () -> new CityLocation("Toronto", 43.6532, 181.0));

    assertThrows(
        IllegalArgumentException.class, () -> new CityLocation("Toronto", 43.6532, -181.0));
  }

  @Test
  void testNonFiniteCoordinates() {
    assertThrows(
        IllegalArgumentException.class, () -> new CityLocation("Toronto", Double.NaN, -79.3832));

    assertThrows(
        IllegalArgumentException.class,
        () -> new CityLocation("Toronto", 43.6532, Double.POSITIVE_INFINITY));
  }

  @Test
  void testBoundaryCoordinates() {
    CityLocation city = new CityLocation("Boundary", 90.0, -180.0);

    assertEquals(90.0, city.getLatitude());
    assertEquals(-180.0, city.getLongitude());
  }

  @Test
  void testDefaultCities() {
    List<CityLocation> cities = CityLocation.defaults();

    assertEquals(6, cities.size());

    List<String> names = cities.stream().map(CityLocation::getName).toList();

    assertEquals(List.of("Toronto", "London", "Tokyo", "New York", "Paris", "Sydney"), names);
  }
}
