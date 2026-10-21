package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public final class GraphGenerator {
    private GraphGenerator() {}

    private static long key(int u, int v, int n) { return (long) u * n + v; }

    public static Graph connectedUndirected(int n, int m, long seed, int maxWeight) {
        if (m < n - 1 || m > (long) n * (n - 1) / 2) throw new IllegalArgumentException("invalid edge count");
        Random rnd = new Random(seed);
        Graph g = new Graph(n, false);
        Set<Long> used = new HashSet<>();
        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, rnd);
        for (int i = 1; i < n; i++) {
            int u = perm.get(i);
            int v = perm.get(rnd.nextInt(i));
            used.add(key(Math.min(u, v), Math.max(u, v), n));
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        while (g.edgeCount() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            long k = key(Math.min(u, v), Math.max(u, v), n);
            if (!used.add(k)) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public static Graph randomDirected(int n, int m, long seed, int maxWeight) {
        if (m > (long) n * (n - 1)) throw new IllegalArgumentException("invalid edge count");
        Random rnd = new Random(seed);
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v || !used.add(key(u, v, n))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }

    public static Graph randomDag(int n, int m, long seed, int maxWeight) {
        if (m > (long) n * (n - 1) / 2) throw new IllegalArgumentException("invalid edge count");
        Random rnd = new Random(seed);
        List<Integer> perm = new ArrayList<>();
        for (int i = 0; i < n; i++) perm.add(i);
        Collections.shuffle(perm, rnd);
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[perm.get(i)] = i;
        Graph g = new Graph(n, true);
        Set<Long> used = new HashSet<>();
        while (g.edgeCount() < m) {
            int u = rnd.nextInt(n), v = rnd.nextInt(n);
            if (u == v) continue;
            if (rank[u] > rank[v]) { int t = u; u = v; v = t; }
            if (!used.add(key(u, v, n))) continue;
            g.addEdge(u, v, 1 + rnd.nextInt(maxWeight));
        }
        return g;
    }
}