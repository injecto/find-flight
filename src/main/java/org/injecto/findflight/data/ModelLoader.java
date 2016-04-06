package org.injecto.findflight.data;

import com.opencsv.CSVReader;
import org.injecto.findflight.model.Flight;
import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.*;

import static java.lang.Math.round;
import static java.lang.Math.toIntExact;

@Singleton
public class ModelLoader {
    private static final Logger log = LoggerFactory.getLogger(ModelLoader.class);

    private static final Random RAND_GEN = new Random();
    private static final double PLANE_VELOCITY = 500.0; // km/h

    private static final String AIRLINES_FILE = "/airlines.dat";
    private static final String AIRPORTS_FILE = "/airports.dat";
    private static final String ROUTES_FILE = "/routes.dat";

    public List<Transfer> loadModel() {
        Map<Integer, Flight> flights = loadFlights();
        Map<Integer, Location> locations = loadLocations();
        return loadTransfers(flights, locations);
    }

    private List<Transfer> loadTransfers(Map<Integer, Flight> flights, Map<Integer, Location> locations) {
        List<Transfer> transfers = new ArrayList<>();
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(ROUTES_FILE)));
        for (String[] line : reader) {
            if (!"0".equals(line[7]))   // ignore multi-hop route
                continue;

            Integer flightId, src, dst;
            try {
                flightId = Integer.valueOf(line[1]);
                if (!flights.containsKey(flightId))
                    throw inconsistentData(line);

                src = Integer.valueOf(line[3]);
                if (!locations.containsKey(src))
                    throw inconsistentData(line);

                dst = Integer.valueOf(line[5]);
                if (!locations.containsKey(dst))
                    throw inconsistentData(line);
            } catch (NumberFormatException e) {
                continue;
            }

            int waitingTime = RAND_GEN.nextInt(181);
            LocalDateTime departureTime = LocalDateTime.of(2016, Month.MAY, 12, RAND_GEN.nextInt(24), RAND_GEN.nextInt(60));
            Location from = locations.get(src);
            Location to = locations.get(dst);
            int duration = calculateDuration(from, to);
            Transfer t = new Transfer(flights.get(flightId), waitingTime, departureTime, duration, from, to);
            transfers.add(t);
        }
        log.info("{} routes was loaded", transfers.size());
        return transfers;
    }

    private static IllegalStateException inconsistentData(String[] line) {
        return new IllegalStateException("Inconsistent route data: " + Arrays.toString(line));
    }

    /**
     * See <a href="https://www.geodatasource.com/developers/java">algorithm source</a>
     */
    private static int calculateDuration(Location from, Location to) {
        double lon1 = from.getLon();
        double lon2 = to.getLon();
        double lat1 = from.getLat();
        double lat2 = to.getLat();

        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344; // kilometers

        return toIntExact(round(dist / PLANE_VELOCITY * 60.0));
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

    private Map<Integer, Flight> loadFlights() {
        Map<Integer, Flight> flights = new HashMap<>();
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(AIRLINES_FILE)));
        for (String[] line : reader) {
            int id = Integer.valueOf(line[0]);
            Flight f = new Flight(id, line[1], BigDecimal.valueOf(RAND_GEN.nextInt(291) + 10));
            flights.put(id, f);
        }
        log.info("{} airlines was loaded", flights.size());
        return flights;
    }

    private Map<Integer, Location> loadLocations() {
        Map<Integer, Location> locations = new HashMap<>();
        CSVReader reader = new CSVReader(new InputStreamReader(getClass().getResourceAsStream(AIRPORTS_FILE)));
        for (String[] line : reader) {
            int id = Integer.valueOf(line[0]);
            String name = line[2] + '-' + line[3];
            Location l = new Location(id, name, Double.valueOf(line[6]), Double.valueOf(line[7]));
            locations.put(id, l);
        }
        log.info("{} airports was loaded", locations.size());
        return locations;
    }
}
