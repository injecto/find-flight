package org.injecto.findflight.util;

import org.injecto.findflight.model.Location;
import org.injecto.findflight.model.Transfer;
import org.jgrapht.graph.MaskFunctor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaskedEdges implements MaskFunctor<Location, Transfer> {
    private static final Logger log = LoggerFactory.getLogger(MaskedEdges.class);
    private final Set<Transfer> maskedEdges;

    public MaskedEdges(List<Transfer> maskedEdges) {
        super();
        this.maskedEdges = new HashSet<>(maskedEdges);
    }

    public MaskedEdges(Transfer maskedEdge) {
        this.maskedEdges = Collections.singleton(maskedEdge);
    }


    @Override
    public boolean isEdgeMasked(Transfer edge) {
        boolean mask = maskedEdges.contains(edge);
        if (mask)
            log.trace("{} was masked", edge);
        return mask;
    }

    @Override
    public boolean isVertexMasked(Location vertex) {
        return false;
    }
}
