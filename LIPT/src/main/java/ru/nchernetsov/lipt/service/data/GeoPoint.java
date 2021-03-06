package ru.nchernetsov.lipt.service.data;

import java.util.Objects;

public class GeoPoint {

    /**
     * "Очищенный" адрес
     */
    private final String cleanAddress;

    /**
     * Географическая широта, десятичные градусы
     */
    private final double lat;

    /**
     * Географическая долгота, десятичные градусы
     */
    private final double lon;

    private GeoPoint(String cleanAddress, double lat, double lon) {
        this.cleanAddress = cleanAddress;
        this.lat = lat;
        this.lon = lon;
    }

    public static GeoPoint of(String cleanAddress, double lat, double lon) {
        return new GeoPoint(cleanAddress, lat, lon);
    }

    public String getCleanAddress() {
        return cleanAddress;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoPoint geoPoint = (GeoPoint) o;
        return Double.compare(geoPoint.lat, lat) == 0 &&
                Double.compare(geoPoint.lon, lon) == 0 &&
                Objects.equals(cleanAddress, geoPoint.cleanAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cleanAddress, lat, lon);
    }

    public String toText() {
        return String.format("Широта: %.2f, долгота: %.2f", lat, lon);
    }
}
