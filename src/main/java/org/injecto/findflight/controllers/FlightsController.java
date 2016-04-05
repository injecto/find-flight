package org.injecto.findflight.controllers;

import org.injecto.findflight.data.ModelLoader;
import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Route;
import org.injecto.findflight.model.Transfer;
import org.injecto.findflight.util.MaskedEdges;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMaskSubgraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.SECONDS;

@Singleton
public class FlightsController {
    private static final Logger log = LoggerFactory.getLogger(FlightsController.class);

    private final ExecutorService executor;
    private final int minChangeTime;
    private final int maxRoutes;
    private final int maxSearchTime;
    private final DirectedGraph<Location, Transfer> graph;
    private final Map<String, Location> locations;

    @Inject
    public FlightsController(@Named("routes.minChangeTime") int minChangeTime, @Named("routes.maxRoutes") int maxRoutes,
                             @Named("routes.maxSearchTime") int maxSearchTime, ModelLoader modelLoader, ExecutorService executor) {
        this.minChangeTime = minChangeTime;
        this.maxRoutes = maxRoutes;
        this.maxSearchTime = maxSearchTime;
        this.executor = executor;

        DirectedGraph<Location, Transfer> graph = new DirectedMultigraph<>(Transfer.class);
        locations = new HashMap<>();
        List<Transfer> transfers = modelLoader.loadModel();
        for (Transfer t : transfers) {
            Location from = t.getFrom();
            Location to = t.getTo();

            locations.putIfAbsent(from.getCanonicalName(), from);
            locations.putIfAbsent(to.getCanonicalName(), to);

            graph.addVertex(from);
            graph.addVertex(to);

            try {
                graph.addEdge(from, to, t);
            } catch (IllegalArgumentException e) {
                log.error("Transfer {} was skipped: {}", t, e.getMessage());
            }
        }
        this.graph = new UnmodifiableDirectedGraph<>(graph);
    }

    public Set<Route> getRoutes(String startLocation, String endLocation) {
        if (!locations.containsKey(startLocation.toLowerCase()))
            throw new IllegalArgumentException("Unknown location `" + startLocation + "`");
        if (!locations.containsKey(endLocation.toLowerCase()))
            throw new IllegalArgumentException("Unknown location `" + endLocation + "`");

        Location from = locations.get(startLocation.toLowerCase());
        Location to = locations.get(endLocation.toLowerCase());
        Future<Set<Route>> routesFuture = executor.submit(() -> getRoutes(from, to));
        try {
            return routesFuture.get(maxSearchTime, SECONDS);
        } catch (TimeoutException | InterruptedException e) {
            boolean cancelled = routesFuture.cancel(true);
            log.warn("Slow routes search, operation was cancelled{}", cancelled ? "" : ", but unsuccessfully");
            return emptySet();
        } catch (ExecutionException e) {
            log.warn("Can't find routes", e);
            return emptySet();
        }
    }

    private Set<Route> getRoutes(Location from, Location to) {
        log.debug("Start routes search for {} -> {}", from, to);

        Set<Route> routes = new HashSet<>();
        Queue<Transfer> canMask = new ArrayDeque<>();
        DirectedGraph<Location, Transfer> original = this.graph;
        DirectedGraph<Location, Transfer> graphView = original;
        while (routes.size() <= maxRoutes && !Thread.currentThread().isInterrupted()) {
            log.debug("Route search attempt");
            List<Transfer> path = DijkstraShortestPath.findPathBetween(graphView, from, to);
            canMask.addAll(path);
            Transfer toMask = canMask.poll();
            if (toMask == null)
                break;

            graphView = new DirectedMaskSubgraph<>(original, new MaskedEdges(toMask));

            if (isRouteValid(path)) {
                Route r = new Route(path);
                boolean isNew = routes.add(r);
                if (isNew)
                    log.debug("Route was found: {}", r);
                else
                    original = new DirectedMaskSubgraph<>(original, new MaskedEdges(path));
            }
        }

        log.debug("Routes search for {} -> {} done ({} routes found)", from, to, routes.size());
        return routes;
    }

    public Set<String> locationsStartedWith(String prefix) {
        return locations.keySet().stream()
                .filter(canonicalName -> canonicalName.startsWith(prefix))
                .collect(Collectors.toSet());
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
