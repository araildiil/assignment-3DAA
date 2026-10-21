package shortestpath;

import graph.Edge;
import graph.Graph;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public final class Dijkstra {
    public static final long INF = Long.MAX_VALUE;

    private Dijkstra() {}

    public static class Result {
        public final int source;
        public final long[] dist;
        public final int[] prev;
        public final long edgesExamined, relaxations, heapPushes, heapPops, comparisons;

        Result(int source, long[] dist, int[] prev, long edgesExamined, long relaxations,
               long heapPushes, long heapPops, long comparisons) {
            this.source = source;
            this.dist = dist;
            this.prev = prev;
            this.edgesExamined = edgesExamined;
            this.relaxations = relaxations;
            this.heapPushes = heapPushes;
            this.heapPops = heapPops;
            this.comparisons = comparisons;
        }

        public boolean isReachable(int v) { return dist[v] != INF; }

        public List<Integer> pathTo(int target) {
            List<Integer> path = new ArrayList<>();
            if (!isReachable(target)) return path;
            for (int v = target; v != -1; v = prev[v]) path.add(v);
            Collections.reverse(path);
            return path;
        }
    }

    public static Result run(Graph g, int source) {
        int n = g.vertexCount();
        if (source < 0 || source >= n) throw new IllegalArgumentException("invalid source");
        for (Edge e : g.edges()) {
            if (e.weight() < 0) throw new IllegalArgumentException("Dijkstra requires non-negative edge weights");
        }
        long[] dist = new long[n];
        int[] prev = new int[n];
        Arrays.fill(dist, INF);
        Arrays.fill(prev, -1);
        boolean[] done = new boolean[n];
        long[] cmp = {0};
        PriorityQueue<long[]> pq = new PriorityQueue<>((a, b) -> {
            cmp[0]++;
            return Long.compare(a[0], b[0]);
        });
        long examined = 0, relax = 0, pushes = 1, pops = 0;
        dist[source] = 0;
        pq.add(new long[]{0, source});
        while (!pq.isEmpty()) {
            long[] top = pq.poll();
            pops++;
            int u = (int) top[1];
            if (done[u]) continue;
            done[u] = true;
            for (Edge e : g.neighbors(u)) {
                examined++;
                long nd = dist[u] + e.weight();
                if (nd < dist[e.to()]) {
                    dist[e.to()] = nd;
                    prev[e.to()] = u;
                    pq.add(new long[]{nd, e.to()});
                    pushes++;
                    relax++;
                }
            }
        }
        return new Result(source, dist, prev, examined, relax, pushes, pops, cmp[0]);
    }
}