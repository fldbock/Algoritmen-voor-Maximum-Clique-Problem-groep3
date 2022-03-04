import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;
import java.lang.*;

public class Ostergard {
    private static boolean gevonden;
    private static int max;
    private static int[] c;
    private static List<Node> toppen;

    public static int Ostergard(UndirectedGraph G) {
        // we maken een Comparator aan om onze toppen te kunnen sorteren op graad
        Comparator<Node> nodeComparator = (node1, node2) -> {
            int n1 = G.getDegree(node1);
            int n2 = G.getDegree(node2);
            return Integer.compare(n1, n2);
        };
        toppen = G.getAllNodes();
        toppen.sort(nodeComparator);

        c = new int[toppen.size()];
        c[c.length - 1] = 1;
        int max = 0;

        for (int i = toppen.size() - 2; i >= 0; i--) {
            // U bestaat in eerste instantie uit alle toppen {v_j| j>=i} en daarna wordt de doorsnede van deze lijst
            // en alle buren van v_i genomen
            ArrayList<Node> U = new ArrayList<>(toppen.subList(i, toppen.size() - 1));
            U.retainAll(G.getNeighbours(toppen.get(i)));
            gevonden = false;
            maxKliek(G, U, 1);
            c[i] = max;
        }
        return c[0];
    }

    // we maken een privaat backtracking algoritme.
    private static boolean maxKliek(UndirectedGraph G, List<Node> U, int size) {
        while (!U.isEmpty()) {
            if (size + U.size() <= max) {
                return gevonden;
            }
            // we zoeken i = min{j|v_j \in U}
            boolean min = false;
            int i = 0;
            while (!min) {
                if (U.contains(toppen.get(i))) {
                    min = true;
                }
                i++;
            }
            i--;
            // we hebben nu i = min{j|v_j \in U}
            if (size + c[i] <= max) {
                return false;
            }
            U.remove(toppen.get(i));
            U.retainAll(G.getNeighbours(toppen.get(i)));
            maxKliek(G, U, size + 1);
            if (gevonden) {return gevonden;}
        }
        if (U.isEmpty()) {
            if (size > max) {
                max = size;
                gevonden = true;
                return gevonden;
            }
        }
        return gevonden;
    }


    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int beste = Ostergard(graph);
            System.out.println(testFiles.get(i) + ": " + beste + ": " + String.valueOf(System.currentTimeMillis() - startTime));
        }
    }
}
