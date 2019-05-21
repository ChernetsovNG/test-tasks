package ru.nchernetsov.lipt.service.data;

import java.util.Objects;

public class GeoPoint {

    /**
     * Географическая широта, десятичные градусы
     */
    private final double lat;

    /**
     * Географическая долгота, десятичные градусы
     */
    private final double lon;

    private GeoPoint(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public static GeoPoint of(double lat, double lon) {
        return new GeoPoint(lat, lon);
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
                Double.compare(geoPoint.lon, lon) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public String toString() {
        return "GeoPoint{" +
                "lat=" + lat +
                ", lon=" + lon +
                '}';
    }
}
