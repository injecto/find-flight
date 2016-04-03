package org.injecto.findflight.model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Transfer {
    private final Flight flight;
    private final int waitingTime;
    private final LocalDateTime departureTime;
    private final int duration;

    private final Location from;
    private final Location to;

    public Transfer(Flight flight, int waitingTime, LocalDateTime departureTime, int duration, Location from, Location to) {
        this.flight = flight;
        this.waitingTime = waitingTime;
        this.departureTime = departureTime;
        this.duration = duration;
        this.from = from;
        this.to = to;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public int getDuration() {
        return duration;
    }

    public Flight getFlight() {
        return flight;
    }

    public Location getFrom() {
        return from;
    }

    public Location getTo() {
        return to;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public LocalDateTime getArrivalTime() {
        return departureTime.plusMinutes(duration);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transfer transfer = (Transfer) o;
        return Objects.equals(flight, transfer.flight) &&
                Objects.equals(from, transfer.from) &&
                Objects.equals(to, transfer.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flight, from, to);
    }

    @Override
    public String toString() {
        return from + " " + waitingTime + " -" + duration + "->" + to + " (" + flight + ")";
    }
}
