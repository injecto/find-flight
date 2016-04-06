package org.injecto.findflight.handlers;

import org.injecto.findflight.model.Graph;
import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
public class RoutesHandler implements spark.Route {
    private static final Logger log = LoggerFactory.getLogger(RoutesHandler.class);

    private final int maxSearchTime;
    private final ExecutorService executor;
    private final Graph graph;

    private enum Order {
        CHANGES(Comparator.comparingInt(Route::getChangesNum)),
        DURATION(Comparator.comparingInt(Route::getDuration)),
        COST(Comparator.comparing(Route::getCost, Comparator.naturalOrder()));

        private final Comparator<Route> comparator;

        Order(Comparator<Route> comparator) {
            this.comparator = comparator;
        }

        static Order from(@Nullable String str) {
            if (str != null) {
                for (Order o : Order.values()) {
                    if (o.name().equalsIgnoreCase(str))
                        return o;
                }
            }
            return COST;
        }
    }

    @Inject
    public RoutesHandler(@Named("routes.maxSearchTime") int maxSearchTime, ExecutorService executor, Graph graph) {
        this.maxSearchTime = maxSearchTime;
        this.executor = executor;
        this.graph = graph;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String from = request.params(":from").toLowerCase();
        String to = request.params(":to").toLowerCase();
        Order order = Order.from(request.queryParams("order"));
        boolean descending = request.queryParams("desc") != null;

        if (!(graph.locationExist(from) && graph.locationExist(to)))
            throw new IllegalArgumentException("Unknown location(s)");

        Location src = graph.getLocation(from);
        Location dst = graph.getLocation(to);

        Future<Set<Route>> routesFuture = executor.submit(() -> graph.findRoutes(src, dst));
        response.header("Content-Type", "application/json");

        try {
            return routesFuture.get(maxSearchTime + 1, SECONDS).stream()
                    .sorted(descending ? order.comparator.reversed() : order.comparator)
                    .collect(Collectors.toList());
        } catch (TimeoutException | InterruptedException e) {
            routesFuture.cancel(true);
            log.warn("Slow routes search, operation was cancelled");
            return emptySet();
        } catch (ExecutionException e) {
            log.warn("Can't find routes", e);
            return emptySet();
        }
    }
}
