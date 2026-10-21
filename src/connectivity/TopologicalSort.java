package connectivity;

import graph.Edge;
import graph.Graph;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public final class TopologicalSort {
    private TopologicalSort() {}

    public static class Result {
        public final boolean isDag;
        public final List<Integer> order;
        public final int processed;
        public final long edgesExamined;

        Result(boolean isDag, List<Integer> order, int processed, long edgesExamined) {
            this.isDag = isDag;
            this.order = order;
            this.processed = processed;
            this.edgesExamined = edgesExamined;
        }
    }

    public static Result run(Graph g) {
        if (!g.isDirected()) throw new IllegalArgumentException("Topological order requires a directed graph");
        int n = g.vertexCount();
        int[] indeg = new int[n];
        for (Edge e : g.edges()) indeg[e.to()]++;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        for (int v = 0; v < n; v++) if (indeg[v] == 0) queue.add(v);
        List<Integer> order = new ArrayList<>();
        long examined = 0;
        while (!queue.isEmpty()) {
            int u = queue.poll();
            order.add(u);
            for (Edge e : g.neighbors(u)) {
                examined++;
                if (--indeg[e.to()] == 0) queue.add(e.to());
            }
        }
        boolean dag = order.size() == n;
        int processed = order.size();
        return new Result(dag, dag ? order : new ArrayList<>(), processed, examined);
    }
}