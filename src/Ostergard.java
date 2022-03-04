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
            // U bestaat in eerste instantie uit alle toppen {v_j| j>=i} en daarna wordt de doorsnede van deze lijst
            // en alle buren van v_i genomen
            ArrayList<Node> U = new ArrayList<>(toppen.subList(i+1, toppen.size())); //toppen.subList(i, toppen.size()) = S_i
            // we nemen de doorsnede van U en de buren van v_i
            U.retainAll(G.getNeighbours(toppen.get(i)));
            gevonden = false;
            maxKliek(G, U, 1, toppen);
            // als gevonden = true, dan is er een kliek met v_i die 1 groter is dan de max kliek in S_{i+1}
            if(gevonden){
                max ++;
            }
            c[i] = max;
        }
        return c[0];
    }

    // we maken een privaat backtracking algoritme.
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
            // we hebben nu i = min{j|v_j \in U}, we snoeien opnieuw, als de voorlopige grote plus de max kliek met
            // top v_i kleiner zijn dan het max, dan kunnen we zeker geen grotere kliek dan max bekomen
            if (size + c[i] <= max) {
                return;
            }
            // we weten dat we door v_i toe te voegen mogelijks een nieuwe maximum kliek kunnen krijgen, we doen dit dus
            U.remove(toppen.get(i));
            U.retainAll(G.getNeighbours(toppen.get(i)));
            maxKliek(G, U, size + 1,toppen);
            // als gevonden = true, dan hebben we een kliek gevonden die groter is dan onze max, we kunnen maximaal
            // een kliek vinden die 1 groter is dan max, dus we returnen true, en in de hoofdmethode gaat max ++
            if (gevonden) {return;}
        }
        // als we hier zijn, is U zeker leeg, want anders zaten we nog in de while lus
        if (size > max) {
            gevonden = true;
        }
        // we geven nu terug of we al dan niet een kliek gevonden hebben.
    }

    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int beste = ostergard(graph);
            System.out.println(testFiles.get(i) + ": " + beste + ": " + String.valueOf(System.currentTimeMillis() - startTime));
        }
   }

    /**
     public static void main(String[] args){
     UndirectedGraph graph2 = new UndirectedGraph();
     Node    a = graph2.addNode("1"),
     b = graph2.addNode("2"),
     c = graph2.addNode("3"),
     d = graph2.addNode("4"),
     e = graph2.addNode("5"),
     f = graph2.addNode("6"),
     g = graph2.addNode("7"),
     h = graph2.addNode("8");
     graph2.addEdge(a, c);
     graph2.addEdge(a, e);
     graph2.addEdge(a, f);
     graph2.addEdge(a, h);
     graph2.addEdge(b, e);
     graph2.addEdge(b, f);
     graph2.addEdge(b, g);
     graph2.addEdge(c, h);
     graph2.addEdge(c, f);
     graph2.addEdge(d, h);
     graph2.addEdge(d, g);
     graph2.addEdge(d, f);
     graph2.addEdge(e, h);
     graph2.addEdge(e,g);

     long startTime = System.currentTimeMillis();
     int best = Ostergard(graph2);
     System.out.println(best  + ": " + String.valueOf(System.currentTimeMillis()-startTime));
     }
     */
}
