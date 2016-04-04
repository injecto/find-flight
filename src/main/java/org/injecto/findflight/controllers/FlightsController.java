package org.injecto.findflight.controllers;

import org.injecto.findflight.data.ModelLoader;
import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Route;
import org.injecto.findflight.model.Transfer;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.KShortestPaths;
import org.jgrapht.graph.DirectedMultigraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.MINUTES;

@Singleton
public class FlightsController {
    private static final Logger log = LoggerFactory.getLogger(FlightsController.class);

    private final int minChangeTime;
    private final int maxRoutes;
    private final DirectedGraph<Location, Transfer> graph;
    private final Map<String, Location> locations;

    @Inject
    public FlightsController(@Named("routes.minChangeTime") int minChangeTime, @Named("routes.maxRoutes") int maxRoutes,
                             ModelLoader modelLoader) {
        this.minChangeTime = minChangeTime;
        this.maxRoutes = maxRoutes;

        graph = new DirectedMultigraph<>(Transfer.class);
        locations = new HashMap<>();
        List<Transfer> transfers = modelLoader.loadModel();
        for (Transfer t : transfers) {
            Location from = t.getFrom();
            Location to = t.getTo();

            locations.putIfAbsent(from.getName().toLowerCase(), from);
            locations.putIfAbsent(to.getName().toLowerCase(), to);

            graph.addVertex(from);
            graph.addVertex(to);

            try {
                graph.addEdge(from, to, t);
            } catch (IllegalArgumentException e) {
                log.error("Transfer {} was skipped", t, e);
            }
        }
    }

    public List<Route> getRoutes(String startLocation, String endLocation) {
        if (!locations.containsKey(startLocation.toLowerCase()))
            throw new IllegalArgumentException("Unknown location `" + startLocation + "`");
        if (!locations.containsKey(endLocation.toLowerCase()))
            throw new IllegalArgumentException("Unknown location `" + endLocation + "`");

        KShortestPaths<Location, Transfer> shortestPaths = new KShortestPaths<>(graph,
                locations.get(startLocation.toLowerCase()), maxRoutes);
        List<Route> routes = new ArrayList<>();
        List<GraphPath<Location, Transfer>> paths = shortestPaths.getPaths(locations.get(endLocation.toLowerCase()));

        if (paths != null) {
            for (GraphPath<Location, Transfer> path : paths) {
                List<Transfer> transfers = path.getEdgeList();
                if (isRouteValid(transfers))
                    routes.add(new Route(transfers));
            }
        }
        return routes;
    }

    private boolean isRouteValid(List<Transfer> transfers) {
        if (transfers.size() > 1) {
            for (int i = 1; i < transfers.size(); i++) {
                Transfer prev = transfers.get(i - 1);
                Transfer next = transfers.get(i);
                long delta = MINUTES.between(prev.getArrivalTime(), next.getDepartureTime());

                if (delta < minChangeTime)
                    return false;
            }
        }
        return true;
    }
}
