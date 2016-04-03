package org.injecto.findflight.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.time.temporal.ChronoUnit.MINUTES;

public class Route {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final List<Transfer> transfers;
    private final BigDecimal totalCost;
    private final int changesNum;
    private final int duration;

    public Route(List<Transfer> transfers) {
        if (transfers.isEmpty())
            throw new IllegalArgumentException("Empty route");

        this.transfers = transfers;

        totalCost = transfers.stream()
                .map(Transfer::getFlight)
                .distinct()
                .map(Flight::getCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        changesNum = transfers.size() - 1;

        Transfer fst = transfers.get(0);
        Transfer last = transfers.get(transfers.size() - 1);
        duration = Math.toIntExact(MINUTES.between(fst.getDepartureTime(), last.getArrivalTime()));
    }

    public BigDecimal getCost() {
        return totalCost;
    }

    public int getChangesNum() {
        return changesNum;
    }

    public int getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Transfer start = transfers.get(0);
        sb.append(FORMATTER.format(start.getDepartureTime()))
                .append(' ').append(start.getFrom());

        for (Transfer t : transfers) {
            if (!t.equals(start))
                sb.append(' ').append(FORMATTER.format(t.getDepartureTime()));

            sb.append(" -- ").append(t.getFlight().getName())
                    .append(" --> ").append(FORMATTER.format(t.getArrivalTime()))
                    .append(' ').append(t.getTo());
        }
        return sb.toString();
    }
}
