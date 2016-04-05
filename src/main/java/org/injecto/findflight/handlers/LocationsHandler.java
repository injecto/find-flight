package org.injecto.findflight.handlers;

import org.injecto.findflight.controllers.FlightsController;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocationsHandler implements spark.Route {
    private final FlightsController flightsController;

    @Inject
    public LocationsHandler(FlightsController flightsController) {
        this.flightsController = flightsController;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String prefix = request.params(":prefix");
        response.header("Content-Type", "application/json");
        return flightsController.locationsStartedWith(prefix);
    }
}
