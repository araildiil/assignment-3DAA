package experiments;

import connectivity.SCC;
import connectivity.TopologicalSort;
import dsu.DSU;
import graph.Graph;
import graph.GraphGenerator;
import mst.Kruskal;
import mst.MSTResult;
import mst.Prim;
import shortestpath.Dijkstra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Benchmark {
    static final int REPEATS = 5;
    static final int WARMUP = 20;
    static final long SEED = 42;
    static final int MAX_WEIGHT = 100;
    static final int[][] CONFIGS = {{10, 30}, {100, 500}, {1000, 5000}};
    static final String[] NAMES = {"Small", "Medium", "Large"};

    record Row(String algorithm, String graph, int v, int e, double avgMicros, String ops) {}

    static <T> Row measure(String algo, String graphName, int v, int e,
                           Supplier<T> task, Function<T, String> opsFormatter) {
        for (int i = 0; i < WARMUP; i++) task.get();
        long total = 0;
        T last = null;
        for (int i = 0; i < REPEATS; i++) {
            long start = System.nanoTime();
            last = task.get();
            total += System.nanoTime() - start;
        }
        return new Row(algo, graphName, v, e, total / (double) REPEATS / 1000.0, opsFormatter.apply(last));
    }

    public static void main(String[] args) throws IOException {
        Files.createDirectories(Path.of("results", "tables"));
        Files.createDirectories(Path.of("results", "plots"));
        List<Row> rows = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        for (int i = 0; i < CONFIGS.length; i++) {
            final int v = CONFIGS[i][0], e = CONFIGS[i][1];
            final String name = NAMES[i];
            final Graph und = GraphGenerator.connectedUndirected(v, e, SEED + i, MAX_WEIGHT);
            final Graph dag = GraphGenerator.randomDag(v, e, SEED + i, MAX_WEIGHT);
            final Graph cyc = GraphGenerator.randomDirected(v, e, SEED + i, MAX_WEIGHT);

            rows.add(measure("Kruskal", name, v, e, () -> Kruskal.run(und),
                    r -> String.format("examined=%d, sortCmp=%d, dsuOps=%d, weight=%d",
                            r.edgesExamined, r.comparisons, r.dsuOps, r.totalWeight)));
            rows.add(measure("Prim", name, v, e, () -> Prim.run(und),
                    r -> String.format("examined=%d, heapCmp=%d, heapOps=%d, weight=%d",
                            r.edgesExamined, r.comparisons, r.heapOps, r.totalWeight)));
            rows.add(measure("TopoSort (DAG)", name, v, e, () -> TopologicalSort.run(dag),
                    r -> String.format("examined=%d, isDag=%b, processed=%d", r.edgesExamined, r.isDag, r.processed)));
            rows.add(measure("TopoSort (cyclic)", name, v, e, () -> TopologicalSort.run(cyc),
                    r -> String.format("examined=%d, isDag=%b, processed=%d", r.edgesExamined, r.isDag, r.processed)));
            rows.add(measure("SCC (Tarjan)", name, v, e, () -> SCC.run(cyc),
                    r -> String.format("examined=%d, components=%d", r.edgesExamined, r.components.size())));
            rows.add(measure("Dijkstra", name, v, e, () -> Dijkstra.run(und, 0),
                    r -> String.format("examined=%d, relaxations=%d, pushes=%d, pops=%d, cmp=%d",
                            r.edgesExamined, r.relaxations, r.heapPushes, r.heapPops, r.comparisons)));

            MSTResult k = Kruskal.run(und), p = Prim.run(und);
            SCC.Result scc = SCC.run(cyc);
            notes.add(String.format("%s: undirected connected=%b, Kruskal weight=%d, Prim weight=%d, equal=%b | "
                            + "random directed graph: %d SCCs (has cycles: %b) | DAG graph is acyclic: %b",
                    name, k.connected, k.totalWeight, p.totalWeight, k.totalWeight == p.totalWeight,
                    scc.components.size(), scc.components.size() < v, TopologicalSort.run(dag).isDag));
        }

        writeTables(rows, notes);
        writePlots(rows);
        dsuExperiment();
        System.out.println("Done. See the results/ folder.");
    }

    static void writeTables(List<Row> rows, List<String> notes) throws IOException {
        StringBuilder md = new StringBuilder();
        StringBuilder csv = new StringBuilder("algorithm,graph,V,E,avg_time_us,operations\n");
        md.append("| Algorithm | Graph | V | E | Avg time (us) | Operation counts |\n");
        md.append("|---|---|---|---|---|---|\n");
        for (Row r : rows) {
            md.append(String.format(Locale.US, "| %s | %s | %d | %d | %.2f | %s |%n",
                    r.algorithm(), r.graph(), r.v(), r.e(), r.avgMicros(), r.ops()));
            csv.append(String.format(Locale.US, "%s,%s,%d,%d,%.2f,\"%s\"%n",
                    r.algorithm(), r.graph(), r.v(), r.e(), r.avgMicros(), r.ops()));
        }
        md.append("\nGeneration notes:\n\n");
        for (String n : notes) md.append("- ").append(n).append("\n");
        Files.writeString(Path.of("results", "tables", "results.md"), md.toString());
        Files.writeString(Path.of("results", "tables", "results.csv"), csv.toString());
        System.out.println(md);
    }

    static void writePlots(List<Row> rows) throws IOException {
        Map<String, List<double[]>> byV = new LinkedHashMap<>();
        Map<String, List<double[]>> byE = new LinkedHashMap<>();
        Map<String, List<double[]>> mstE = new LinkedHashMap<>();
        for (Row r : rows) {
            byV.computeIfAbsent(r.algorithm(), x -> new ArrayList<>()).add(new double[]{r.v(), r.avgMicros()});
            byE.computeIfAbsent(r.algorithm(), x -> new ArrayList<>()).add(new double[]{r.e(), r.avgMicros()});
            if (r.algorithm().equals("Kruskal") || r.algorithm().equals("Prim")) {
                mstE.computeIfAbsent(r.algorithm(), x -> new ArrayList<>()).add(new double[]{r.e(), r.avgMicros()});
            }
        }
        PlotWriter.lineChart("results/plots/time_vs_vertices.png", "Execution time vs number of vertices",
                "Vertices V (log scale)", "Average time, microseconds (log scale)", byV);
        PlotWriter.lineChart("results/plots/time_vs_edges.png", "Execution time vs number of edges",
                "Edges E (log scale)", "Average time, microseconds (log scale)", byE);
        PlotWriter.lineChart("results/plots/kruskal_vs_prim.png", "Kruskal vs Prim",
                "Edges E (log scale)", "Average time, microseconds (log scale)", mstE);
    }

    static void dsuExperiment() throws IOException {
        final int n = 20000;
        boolean[][] variants = {{false, false}, {true, false}, {false, true}, {true, true}};
        String[] names = {"naive (no compression, no rank)", "path compression only",
                "union by rank only", "path compression + union by rank"};
        StringBuilder md = new StringBuilder("| DSU variant | n | Time (ms) | Parent-pointer steps |\n|---|---|---|---|\n");
        for (int i = 0; i < variants.length; i++) {
            DSU d = new DSU(n, variants[i][0], variants[i][1]);
            long start = System.nanoTime();
            for (int x = 0; x < n - 1; x++) d.union(x, x + 1);
            for (int x = 0; x < n; x++) d.find(x);
            double ms = (System.nanoTime() - start) / 1e6;
            md.append(String.format(Locale.US, "| %s | %d | %.2f | %d |%n", names[i], n, ms, d.getParentSteps()));
        }
        Files.writeString(Path.of("results", "tables", "dsu.md"), md.toString());
        System.out.println(md);
    }
}