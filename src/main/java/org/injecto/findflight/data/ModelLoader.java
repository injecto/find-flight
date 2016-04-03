package org.injecto.findflight.data;

import org.injecto.findflight.model.Flight;
import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Transfer;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class ModelLoader {
    private static final Logger log = LoggerFactory.getLogger(ModelLoader.class);
    private static final String SAMPLE_DB = "/sample_db.sql";

    private final String dbUrl;

    @Inject
    public ModelLoader(@Named("db.url") String dbUrl) {
        this.dbUrl = dbUrl;
        createSampleDb();
    }

    public List<Transfer> loadModel() {
        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext ctx = DSL.using(connection);

            Result<Record> res = ctx.select().from("flight").fetch();
            Map<Integer, Flight> flights = new HashMap<>(res.size());
            for (Record r : res) {
                Integer id = r.getValue("ID", Integer.class);
                String name = r.getValue("NAME", String.class);
                BigDecimal cost = r.getValue("COST", BigDecimal.class);
                flights.put(id, new Flight(id, name, cost));
            }

            res = ctx.select().from("location").fetch();
            Map<Integer, Location> locations = new HashMap<>(res.size());
            for (Record r : res) {
                Integer id = r.getValue("ID", Integer.class);
                String name = r.getValue("NAME", String.class);
                locations.put(id, new Location(id, name));
            }

            res = ctx.select().from("transfer").fetch();
            List<Transfer> transfers = new ArrayList<>(res.size());
            for (Record r : res) {
                Integer flightId = r.getValue("FLIGHT_ID", Integer.class);
                Integer waitingTime = r.getValue("WAITING_TIME", Integer.class);
                LocalDateTime departureTime = r.getValue("DEPARTURE_TIME", LocalDateTime.class);
                Integer duration = r.getValue("DURATION", Integer.class);
                Integer from = r.getValue("FROM_LOCATION", Integer.class);
                Integer to = r.getValue("TO_LOCATION", Integer.class);

                if (!(flights.containsKey(flightId) && locations.containsKey(from) && locations.containsKey(to))) {
                    String msg = String.format("Inconsistent transfer data: flight %d, from %d, to %d", flightId, from, to);
                    throw new RuntimeException(msg);
                }

                transfers.add(new Transfer(flights.get(flightId), waitingTime, departureTime, duration,
                        locations.get(from), locations.get(to)));
            }

            log.info("Model was loaded ({} transfers total)", transfers.size());
            return transfers;
        } catch (SQLException e) {
            log.error("Can't load model", e);
            throw new RuntimeException(e);
        }
    }

    private void createSampleDb() {
        String sql;
        try {
            URL sampleDbUrl = getClass().getResource(SAMPLE_DB);
            byte[] bs = Files.readAllBytes(Paths.get(sampleDbUrl.toURI()));
            sql = new String(bs);
        } catch (Throwable e) {
            throw new RuntimeException("Can't load sample DB script");
        }

        try (Connection connection = DriverManager.getConnection(dbUrl)) {
            DSLContext ctx = DSL.using(connection);
            ctx.batch(sql).execute();
        } catch (Throwable e) {
            log.error("Can't create sample DB", e);
            throw new RuntimeException(e);
        }
    }
}
