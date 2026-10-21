package mst;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public final class Prim {
    private Prim() {}

    public static MSTResult run(Graph g) {
        if (g.isDirected()) throw new IllegalArgumentException("MST requires an undirected graph");
        int n = g.vertexCount();
        boolean[] visited = new boolean[n];
        long[] comparisons = {0};
        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            comparisons[0]++;
            return Integer.compare(a.weight(), b.weight());
        });
        List<Edge> mst = new ArrayList<>();
        long total = 0, examined = 0, heapOps = 0;
        int components = 0;

        for (int s = 0; s < n; s++) {
            if (visited[s]) continue;
            components++;
            visited[s] = true;
            for (Edge f : g.neighbors(s)) {
                examined++;
                if (!visited[f.to()]) { pq.add(f); heapOps++; }
            }
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                heapOps++;
                if (visited[e.to()]) continue;
                visited[e.to()] = true;
                mst.add(e);
                total += e.weight();
                for (Edge f : g.neighbors(e.to())) {
                    examined++;
                    if (!visited[f.to()]) { pq.add(f); heapOps++; }
                }
            }
        }
        return new MSTResult(total, mst, components <= 1, components, examined, comparisons[0], 0, heapOps);
    }
}