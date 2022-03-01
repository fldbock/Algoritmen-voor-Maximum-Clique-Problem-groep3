import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;
import java.lang.*;

public class Ostergard {
    public int Ostergard(UndirectedGraph G){
        // we maken een Comparator aan om onze toppen te kunnen sorteren op graad
        Comparator<Node> nodeComparator = (node1, node2) -> {
            int n1 = G.getDegree(node1); int n2 = G.getDegree(node2);
            return Integer.compare(n1, n2);
        };
        List<Node> toppen = G.getAllNodes();
        toppen.sort(nodeComparator);
        int[] c = new int[toppen.size()];
        int max = 0;
        for (int i = toppen.size()-1; i>= 0; i--){
            boolean gevonden = false;
            maxKliek(G,  c, 0 , max, gevonden);
            c[i]= max;
        }
        return c[0];
    }

    // we maken een privaat backtracking algoritme.
    private void maxKliek(UndirectedGraph G, int[] c, int size, int max, boolean gevonden){

    }

    // we maken een snoeifunctie op basis van een eenvoudig toppenkleuring algoritme
}
