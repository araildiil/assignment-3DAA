package mst;

import graph.Edge;
import java.util.List;

public class MSTResult {
    public final long totalWeight;
    public final List<Edge> edges;
    public final boolean connected;
    public final int components;
    public final long edgesExamined;
    public final long comparisons;
    public final long dsuOps;
    public final long heapOps;

    public MSTResult(long totalWeight, List<Edge> edges, boolean connected, int components,
                     long edgesExamined, long comparisons, long dsuOps, long heapOps) {
        this.totalWeight = totalWeight;
        this.edges = edges;
        this.connected = connected;
        this.components = components;
        this.edgesExamined = edgesExamined;
        this.comparisons = comparisons;
        this.dsuOps = dsuOps;
        this.heapOps = heapOps;
    }
}