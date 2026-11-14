package catan.entity;

import java.util.List;

public class CityLocation {
    private final String name;
    private final double latitude;
    private final double longitude;

    public CityLocation(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /** The predefined city list. */
    public static List<CityLocation> defaults() {
        return List.of(
                new CityLocation("Toronto", 43.6532, -79.3832),
                new CityLocation("London", 51.5072, -0.1276),
                new CityLocation("Tokyo", 35.6762, 139.6503),
                new CityLocation("New York", 40.7128, -74.0060));
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
