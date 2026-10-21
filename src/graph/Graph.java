package graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private final int n;
    private final boolean directed;
    private final List<List<Edge>> adj = new ArrayList<>();
    private final List<Edge> edges = new ArrayList<>();

    public Graph(int n, boolean directed) {
        if (n < 0) throw new IllegalArgumentException("n must be non-negative");
        this.n = n;
        this.directed = directed;
        for (int i = 0; i < n; i++) adj.add(new ArrayList<>());
    }

    public void addEdge(int u, int v, int w) {
        check(u);
        check(v);
        Edge e = new Edge(u, v, w);
        edges.add(e);
        adj.get(u).add(e);
        if (!directed) adj.get(v).add(new Edge(v, u, w));
    }

    private void check(int v) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("vertex out of range: " + v);
    }

    public int vertexCount() { return n; }
    public int edgeCount() { return edges.size(); }
    public boolean isDirected() { return directed; }
    public List<Edge> neighbors(int u) { return adj.get(u); }
    public List<Edge> edges() { return edges; }
}