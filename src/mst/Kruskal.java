package mst;

import dsu.DSU;
import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;

public final class Kruskal {
    private Kruskal() {}

    public static MSTResult run(Graph g) {
        if (g.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
        int n = g.vertexCount();
        List<Edge> sorted = new ArrayList<>(g.edges());
        long[] comparisons = {0};
        sorted.sort((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight(), b.weight());
        });
        DSU dsu = new DSU(n);
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0;
        for (Edge e : sorted) {
            if (mst.size() == n - 1) break;
            examined++;
            if (dsu.union(e.from(), e.to())) {
                mst.add(e);
                total += e.weight();
            }
        }
        long dsuOps = dsu.getFindCalls() + dsu.getUnionCalls();
        return new MSTResult(total, mst, dsu.componentCount() <= 1, dsu.componentCount(),
                examined, comparisons[0], dsuOps, 0);
    }
}