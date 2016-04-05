package org.injecto.findflight;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.injecto.findflight.handlers.LocationsHandler;
import org.injecto.findflight.handlers.RoutesHandler;
import org.injecto.findflight.util.LocalDateTimeSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.time.LocalDateTime;

import static spark.Spark.*;

@Singleton
public class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);

    private final RoutesHandler routesHandler;
    private final LocationsHandler locationsHandler;
    private int port;
    private final Gson gson;

    @Inject
    public Server(RoutesHandler routesHandler, LocationsHandler locationsHandler, @Named("server.port") int port) {
        this.routesHandler = routesHandler;
        this.locationsHandler = locationsHandler;
        this.port = port;
        gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .create();
    }

    public void start() {
        log.info("Start server");
        port(port);
        get("/routes/:from/:to/", routesHandler, gson::toJson);
        get("/locations/:prefix/", locationsHandler, gson::toJson);
        after((req, resp) -> {
            resp.header("Content-Encoding", "gzip");
        });
    }

    public void stop() {
        log.info("Stop server");
        Spark.stop();
    }
}
