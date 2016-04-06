package org.injecto.findflight.model;

import org.injecto.findflight.data.ModelLoader;
import org.injecto.findflight.util.MaskedEdges;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DirectedMaskSubgraph;
import org.jgrapht.graph.DirectedMultigraph;
import org.jgrapht.graph.UnmodifiableDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.System.currentTimeMillis;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.util.Collections.unmodifiableMap;

@Singleton
public class Graph {
    private static final Logger log = LoggerFactory.getLogger(Graph.class);

    private final Map<String, Location> locations;
    private final DirectedGraph<Location, Transfer> graph;
    private final int minChangeTime;
    private final int maxRoutes;
    private final int maxSearchTime;

    @Inject
    public Graph(ModelLoader modelLoader, @Named("routes.minChangeTime") int minChangeTime,
                 @Named("routes.maxRoutes") int maxRoutes, @Named("routes.maxSearchTime") int maxSearchTime) {
        this.minChangeTime = minChangeTime;
        this.maxRoutes = maxRoutes;
        this.maxSearchTime = maxSearchTime;

        DirectedGraph<Location, Transfer> graph = new DirectedMultigraph<>(Transfer.class);
        Map<String, Location> locations = new HashMap<>();
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
        this.locations = unmodifiableMap(locations);
        this.graph = new UnmodifiableDirectedGraph<>(graph);
    }

    public boolean locationExist(String canonicalName) {
        return locations.containsKey(canonicalName);
    }

    @Nullable
    public Location getLocation(String canonicalName) {
        return locations.get(canonicalName);
    }

    /**
     * Note: linear complexity
     */
    public Set<String> getLocationCanonicalNamesByPrefix(String prefix) {
        return locations.keySet().stream()
                .filter(canonicalName -> canonicalName.startsWith(prefix))
                .collect(Collectors.toSet());
    }

    public Set<Route> findRoutes(Location from, Location to) {
        log.debug("Start routes search for {} -> {}", from, to);

        Set<Route> routes = new HashSet<>();
        Queue<Transfer> canMask = new ArrayDeque<>();
        DirectedGraph<Location, Transfer> model = this.graph;
        DirectedGraph<Location, Transfer> attemptView = model;
        long threshold = currentTimeMillis() + maxSearchTime * 1000;

        while (routes.size() < maxRoutes && !Thread.currentThread().isInterrupted() && currentTimeMillis() < threshold) {
            log.trace("Route search attempt");
            List<Transfer> path = DijkstraShortestPath.findPathBetween(attemptView, from, to);
            canMask.addAll(path);
            Transfer toMask = canMask.poll();
            if (toMask == null)
                break;

            attemptView = new DirectedMaskSubgraph<>(model, new MaskedEdges(toMask));

            if (isRouteValid(path)) {
                Route r = new Route(path);
                boolean isNew = routes.add(r);
                if (isNew) {
                    log.debug("Route was found: {}", r);
                } else {
                    model = new DirectedMaskSubgraph<>(model, new MaskedEdges(path));
                }
            }
        }

        log.debug("Routes search for {} -> {} done ({} routes found)", from, to, routes.size());
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
