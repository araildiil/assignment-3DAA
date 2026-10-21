package tests;

import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Edge;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestRunner {
    static int passed = 0, failed = 0;

    static void check(boolean cond, String name) {
        if (cond) { passed++; System.out.println("[PASS] " + name); }
        else { failed++; System.out.println("[FAIL] " + name); }
    }

    public static void main(String[] args) {
        testDsu();
        testSingleVertex();
        testKnownMst();
        testDisconnected();
        testMultipleMsts();
        testDuplicateWeights();
        testRandomMst();
        testTopological();
        testScc();
        testDijkstra();
        System.out.println("\nPassed: " + passed + ", Failed: " + failed);
        if (failed > 0) System.exit(1);
    }

    static void testDsu() {
        DSU d = new DSU(5);
        d.union(0, 1);
        d.union(1, 2);
        check(d.connected(0, 2), "DSU: 0 and 2 connected after unions");
        check(!d.connected(0, 3), "DSU: 0 and 3 not connected");
        check(!d.union(0, 2), "DSU: union of same set returns false");
        check(d.componentCount() == 3, "DSU: component count is 3");
    }

    static void testSingleVertex() {
        Graph u = new Graph(1, false);
        MSTResult k = Kruskal.run(u), p = Prim.run(u);
        check(k.totalWeight == 0 && k.connected && k.edges.isEmpty(), "Single vertex: Kruskal");
        check(p.totalWeight == 0 && p.connected && p.edges.isEmpty(), "Single vertex: Prim");
        Graph d = new Graph(1, true);
        check(TopologicalSort.run(d).isDag && TopologicalSort.run(d).order.equals(List.of(0)), "Single vertex: topological order");
        check(SCC.run(d).components.size() == 1, "Single vertex: one SCC");
        check(Dijkstra.run(d, 0).dist[0] == 0, "Single vertex: Dijkstra distance 0");
    }

    static void testKnownMst() {
        Graph g = new Graph(4, false);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 2);
        g.addEdge(2, 3, 3);
        g.addEdge(0, 3, 4);
        g.addEdge(0, 2, 5);
        check(Kruskal.run(g).totalWeight == 6, "Known graph: Kruskal weight 6");
        check(Prim.run(g).totalWeight == 6, "Known graph: Prim weight 6");
        check(Kruskal.run(g).connected && Prim.run(g).connected, "Known graph: connected");
    }

    static void testDisconnected() {
        Graph g = new Graph(5, false);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 2, 3);
        g.addEdge(3, 4, 1);
        MSTResult k = Kruskal.run(g), p = Prim.run(g);
        check(!k.connected && !p.connected, "Disconnected: detected by both algorithms");
        check(k.components == 2 && p.components == 2, "Disconnected: 2 components");
        check(k.totalWeight == 6 && p.totalWeight == 6, "Disconnected: forest weight 6 for both");
    }

    static void testMultipleMsts() {
        Graph tri = new Graph(3, false);
        tri.addEdge(0, 1, 1);
        tri.addEdge(1, 2, 1);
        tri.addEdge(0, 2, 1);
        check(Kruskal.run(tri).totalWeight == 2 && Prim.run(tri).totalWeight == 2, "Multiple MSTs: triangle weight 2");
        Graph sq = new Graph(4, false);
        sq.addEdge(0, 1, 5);
        sq.addEdge(1, 2, 5);
        sq.addEdge(2, 3, 5);
        sq.addEdge(3, 0, 5);
        check(Kruskal.run(sq).totalWeight == 15 && Prim.run(sq).totalWeight == 15, "Multiple MSTs: square weight 15");
    }

    static void testDuplicateWeights() {
        boolean ok = true;
        for (int seed = 0; seed < 200; seed++) {
            Graph g = GraphGenerator.connectedUndirected(30, 80, seed, 3);
            if (Kruskal.run(g).totalWeight != Prim.run(g).totalWeight) ok = false;
        }
        check(ok, "Duplicate weights (weights 1..3): Kruskal == Prim on 200 graphs");
    }

    static void testRandomMst() {
        boolean same = true, tree = true;
        for (int seed = 0; seed < 50; seed++) {
            int n = 50 + seed;
            Graph g = GraphGenerator.connectedUndirected(n, 3 * n, seed, 100);
            MSTResult k = Kruskal.run(g), p = Prim.run(g);
            if (k.totalWeight != p.totalWeight || !k.connected || !p.connected) same = false;
            for (MSTResult r : List.of(k, p)) {
                DSU d = new DSU(n);
                if (r.edges.size() != n - 1) tree = false;
                for (Edge e : r.edges) if (!d.union(e.from(), e.to())) tree = false;
            }
        }
        check(same, "Random connected graphs: Kruskal == Prim weight, both connected");
        check(tree, "Random connected graphs: both MSTs have V-1 edges and no cycle");
    }

    static void testTopological() {
        boolean valid = true;
        for (int seed = 0; seed < 50; seed++) {
            Graph g = GraphGenerator.randomDag(40, 100, seed, 10);
            TopologicalSort.Result r = TopologicalSort.run(g);
            if (!r.isDag || r.order.size() != 40) { valid = false; continue; }
            int[] pos = new int[40];
            for (int i = 0; i < 40; i++) pos[r.order.get(i)] = i;
            for (Edge e : g.edges()) if (pos[e.from()] >= pos[e.to()]) valid = false;
        }
        check(valid, "Topological order valid for 50 random DAGs");

        Graph c = new Graph(3, true);
        c.addEdge(0, 1, 1);
        c.addEdge(1, 2, 1);
        c.addEdge(2, 0, 1);
        check(!TopologicalSort.run(c).isDag, "Cycle 0->1->2->0 detected");

        Graph partial = new Graph(4, true);
        partial.addEdge(0, 1, 1);
        partial.addEdge(1, 2, 1);
        partial.addEdge(2, 1, 1);
        partial.addEdge(2, 3, 1);
        check(!TopologicalSort.run(partial).isDag, "Cycle inside a larger graph detected");
    }

    static List<List<Integer>> normalize(List<List<Integer>> comps) {
        List<List<Integer>> out = new ArrayList<>();
        for (List<Integer> c : comps) {
            List<Integer> copy = new ArrayList<>(c);
            Collections.sort(copy);
            out.add(copy);
        }
        out.sort((a, b) -> Integer.compare(a.get(0), b.get(0)));
        return out;
    }

    static void testScc() {
        Graph g = new Graph(6, true);
        g.addEdge(0, 1, 1);
        g.addEdge(1, 2, 1);
        g.addEdge(2, 0, 1);
        g.addEdge(2, 3, 1);
        g.addEdge(3, 4, 1);
        g.addEdge(4, 3, 1);
        List<List<Integer>> expected = List.of(List.of(0, 1, 2), List.of(3, 4), List.of(5));
        check(normalize(SCC.run(g).components).equals(expected), "SCC: known graph {0,1,2},{3,4},{5}");

        boolean exactlyOnce = true;
        for (int seed = 0; seed < 30; seed++) {
            Graph r = GraphGenerator.randomDirected(60, 120, seed, 5);
            boolean[] seen = new boolean[60];
            for (List<Integer> comp : SCC.run(r).components)
                for (int v : comp) { if (seen[v]) exactlyOnce = false; seen[v] = true; }
            for (boolean s : seen) if (!s) exactlyOnce = false;
        }
        check(exactlyOnce, "SCC: every vertex in exactly one component (30 random graphs)");

        Graph dag = GraphGenerator.randomDag(50, 100, 7, 5);
        check(SCC.run(dag).components.size() == 50, "SCC: DAG has V singleton components");
    }

    static long[] bellmanFord(Graph g, int s) {
        int n = g.vertexCount();
        long[] d = new long[n];
        Arrays.fill(d, Dijkstra.INF);
        d[s] = 0;
        for (int i = 0; i < n - 1; i++)
            for (Edge e : g.edges())
                if (d[e.from()] != Dijkstra.INF && d[e.from()] + e.weight() < d[e.to()])
                    d[e.to()] = d[e.from()] + e.weight();
        return d;
    }

    static void testDijkstra() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 1, 4);
        g.addEdge(0, 2, 1);
        g.addEdge(2, 1, 2);
        g.addEdge(1, 3, 1);
        g.addEdge(2, 3, 5);
        Dijkstra.Result r = Dijkstra.run(g, 0);
        check(r.dist[1] == 3 && r.dist[2] == 1 && r.dist[3] == 4, "Dijkstra: known distances");
        check(r.pathTo(3).equals(List.of(0, 2, 1, 3)), "Dijkstra: known path 0->2->1->3");
        check(!r.isReachable(4) && r.pathTo(4).isEmpty(), "Dijkstra: unreachable vertex handled");

        boolean ok = true;
        for (int seed = 0; seed < 30; seed++) {
            Graph rg = GraphGenerator.randomDirected(50, 200, seed, 20);
            if (!Arrays.equals(Dijkstra.run(rg, 0).dist, bellmanFord(rg, 0))) ok = false;
        }
        check(ok, "Dijkstra == Bellman-Ford on 30 random graphs");

        Graph neg = new Graph(2, true);
        neg.addEdge(0, 1, -1);
        boolean threw = false;
        try { Dijkstra.run(neg, 0); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "Dijkstra: rejects negative weights");
    }
}