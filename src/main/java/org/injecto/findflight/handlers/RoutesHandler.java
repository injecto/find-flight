package org.injecto.findflight.handlers;

import org.injecto.findflight.controllers.FlightsController;
import org.injecto.findflight.model.Route;
import spark.Request;
import spark.Response;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class RoutesHandler implements spark.Route {
    private final FlightsController flightsController;

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
    public RoutesHandler(FlightsController flightsController) {
        this.flightsController = flightsController;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String from = request.params(":from");
        String to = request.params(":to");
        Order order = Order.from(request.queryParams("order"));
        boolean descending = request.queryParams("desc") != null;

        try {
            List<Route> routes = flightsController.getRoutes(from, to).stream()
                    .sorted(descending ? order.comparator.reversed() : order.comparator)
                    .collect(Collectors.toList());
            response.header("Content-Type", "application/json");
            return routes;
        } catch (IllegalArgumentException e) {
            response.status(404);
            return e.getMessage();
        }
    }
}
