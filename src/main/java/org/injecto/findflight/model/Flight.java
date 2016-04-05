package org.injecto.findflight.model;

import java.math.BigDecimal;
import java.util.Objects;


public class Flight {
    private final int id;
    private final String name;
    private final BigDecimal cost;

    public Flight(int id, String name, BigDecimal cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        return id == flight.id;
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
