package catan.entity;

import java.util.List;

/**
 * Represents a geographic city used for weather selection. Each city has a name, latitude, and
 * longitude.
 */
public final class CityLocation {

  private static final double MIN_LATITUDE = -90.0;
  private static final double MAX_LATITUDE = 90.0;
  private static final double MIN_LONGITUDE = -180.0;
  private static final double MAX_LONGITUDE = 180.0;

  private final String name;
  private final double latitude;
  private final double longitude;

  /**
   * Constructs a city with its name and geographic coordinates.
   *
   * @param name the name of the city
   * @param latitude the latitude of the city
   * @param longitude the longitude of the city
   * @throws IllegalArgumentException if the name or coordinates are invalid
   */
  public CityLocation(String name, double latitude, double longitude) {
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("City name cannot be empty");
    }

    if (!Double.isFinite(latitude) || latitude < MIN_LATITUDE || latitude > MAX_LATITUDE) {
      throw new IllegalArgumentException("Invalid latitude");
    }

    if (!Double.isFinite(longitude) || longitude < MIN_LONGITUDE || longitude > MAX_LONGITUDE) {
      throw new IllegalArgumentException("Invalid longitude");
    }

    this.name = name;
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Returns the predefined cities available for weather selection.
   *
   * @return a list of predefined city locations
   */
  public static List<CityLocation> defaults() {
    return List.of(
        new CityLocation("Toronto", 43.6532, -79.3832),
        new CityLocation("London", 51.5072, -0.1276),
        new CityLocation("Tokyo", 35.6762, 139.6503),
        new CityLocation("New York", 40.7128, -74.0060),
        new CityLocation("Paris", 48.8566, 2.3522),
        new CityLocation("Sydney", -33.8688, 151.2093));
  }

  /**
   * Returns the name of the city.
   *
   * @return the city name
   */
  public String getName() {
    return name;
  }

  /**
   * Returns the latitude of the city.
   *
   * @return the latitude
   */
  public double getLatitude() {
    return latitude;
  }

  /**
   * Returns the longitude of the city.
   *
   * @return the longitude
   */
  public double getLongitude() {
    return longitude;
  }
}
