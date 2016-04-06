package org.injecto.findflight.handlers;

import org.injecto.findflight.model.Graph;
import spark.Request;
import spark.Response;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class LocationsHandler implements spark.Route {
    private final Graph graph;

    @Inject
    public LocationsHandler(Graph graph) {
        this.graph = graph;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        String prefix = request.params(":prefix");
        response.header("Content-Type", "application/json");
        return graph.getLocationCanonicalNamesByPrefix(prefix);
    }
}
