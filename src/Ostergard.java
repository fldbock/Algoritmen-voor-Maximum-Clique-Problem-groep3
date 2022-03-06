import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;
import java.lang.*;

public class Ostergard {
    private static boolean gevonden;
    private static int max;
    private static int[] c;

    public static int ostergard(UndirectedGraph G) {
        // we maken een Comparator aan om onze toppen te kunnen sorteren op graad
        Comparator<Node> nodeComparator = (node1, node2) -> {
            int n1 = G.getDegree(node1); int n2 = G.getDegree(node2);
            return Integer.compare(n1, n2);
        };
        ArrayList<Node> toppen = new ArrayList<>(G.getAllNodes());
        toppen.sort(nodeComparator);
        // we maken de lijst aan waarin we de maxkliek in S_i bepalen en initialiseren S_n op 1
        c = new int[toppen.size()];
        c[c.length - 1] = 1;
        max = 1;
        // voor elke S_i berekenen we nu de maximum kliek, en de grootte ervan slaan we op in C_i
        for (int i = toppen.size() - 2; i >= 0; i--) {
            // We initialiseren U op de doorsnede van S_i en de buren van v_i
            ArrayList<Node> U = new ArrayList<>(toppen.subList(i+1, toppen.size())); //toppen.subList(i, toppen.size()) = S_i
            U.retainAll(G.getNeighbours(toppen.get(i)));
            gevonden = false;
            maxKliek(G, U, 1, toppen);
            // als gevonden = true, dan is er een kliek met v_i die 1 groter is dan de max kliek in S_{i+1}
            if(gevonden){ max ++; }
            c[i] = max;
        }
        return c[0];
    }

    // we maken een privaat recursief algoritme.
    private static void maxKliek(UndirectedGraph G, List<Node> U, int size, List<Node> toppen) {
        while (!U.isEmpty()) {
            // we beginnen met een snoeifunctie
            if (size + U.size() <= max) {
                return;
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
            // we hebben nu i = min{j|v_j \in U}, we snoeien opnieuw, als de voorlopige grootte plus de max kliek uit
            // S_i kleiner is dan het max, dan kunnen we zeker geen grotere kliek dan max bekomen
            if (size + c[i] <= max) {
                return;
            }
            // we weten dat we door v_i toe te voegen mogelijks een nieuwe maximum kliek kunnen krijgen, we doen dit dus
            U.remove(toppen.get(i));
            ArrayList <Node> newU = new ArrayList<Node>(U);
            newU.retainAll(G.getNeighbours(toppen.get(i)));
            maxKliek(G, newU, size + 1,toppen);
            // als gevonden = true, dan hebben we een kliek gevonden die groter is dan onze max, we kunnen maximaal
            // een kliek vinden die 1 groter is dan max, dus we returnen true, en in de hoofdmethode gaat max ++
            if (gevonden) {return;} // als gevonden=false, herstart de while en proberen we met het volgende elt uit U
        }
        // als we hier zijn, is U zeker leeg, want anders zaten we nog in de while lus
        if (size > max) {
            gevonden = true;
        }
    }

    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5"*/ "C2000.5", /*"brock200_2", "brock200_4",*/ "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int beste = ostergard(graph);
            System.out.println(testFiles.get(i) + ": " + beste + ": " + String.valueOf(System.currentTimeMillis() - startTime));
        }
   }
}
