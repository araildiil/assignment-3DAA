package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class SCC {
    private final Graph g;
    private final int[] index, low;
    private final boolean[] onStack;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final List<List<Integer>> components = new ArrayList<>();
    private int counter = 0;
    private long edgesExamined = 0;

    private SCC(Graph g) {
        this.g = g;
        int n = g.vertexCount();
        index = new int[n];
        low = new int[n];
        onStack = new boolean[n];
        Arrays.fill(index, -1);
    }

    public static class Result {
        public final List<List<Integer>> components;
        public final long edgesExamined;

        Result(List<List<Integer>> components, long edgesExamined) {
            this.components = components;
            this.edgesExamined = edgesExamined;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("SCC requires a directed graph");
        SCC s = new SCC(g);
        for (int v = 0; v < g.vertexCount(); v++) if (s.index[v] == -1) s.dfs(v);
        return new Result(s.components, s.edgesExamined);
    }

    private void dfs(int u) {
        index[u] = low[u] = counter++;
        stack.push(u);
        onStack[u] = true;
        for (Edge e : g.neighbors(u)) {
            edgesExamined++;
            int v = e.to();
            if (index[v] == -1) {
                dfs(v);
                low[u] = Math.min(low[u], low[v]);
            } else if (onStack[v]) {
                low[u] = Math.min(low[u], index[v]);
            }
        }
        if (low[u] == index[u]) {
            List<Integer> comp = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack[w] = false;
                comp.add(w);
            } while (w != u);
            components.add(comp);
        }
    }
}