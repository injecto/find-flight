package org.injecto.findflight.model;

import java.util.Objects;

public class Location {
    private final int id;
    private final String name;
    private final double lat;
    private final double lon;

    public Location(int id, String name, double lat, double lon) {
        this.id = id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCanonicalName() {
        return name.replaceAll("\\s|\\W", "-").toLowerCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return id == location.id;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name;
    }
}
