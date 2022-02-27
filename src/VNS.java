import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

public class VNS {

    private int kmax = 5;

    // Greedy approach that selects node with biggest degree
    public List<Node> greedy (UndirectedGraph G) {
        List<Node> bestClique = new ArrayList<>();
        // Map nodes to number of neighbors
        Map<Node, Integer> neighbors = new HashMap<>();
        for(Node node: G.getAllNodes()){
            neighbors.put(node, G.getDegree(node));
        }
        while (!neighbors.isEmpty()) {
            // Zoek top met hoogste graad
            Node popularNode = Collections.max(neighbors.entrySet(), Map.Entry.comparingByValue()).getKey();
            bestClique.add(popularNode);
            for (Node node: neighbors.keySet()) {
                if (G.getNeighbours(popularNode).contains(node)) {
                    // 1 neighbor minder
                    neighbors.put(node,neighbors.get(node) -1);
                } else {
                    neighbors.remove(node);
                }
            }
            neighbors.remove(popularNode);
        }
        return bestClique;
    }

    public List<Node> VNS (UndirectedGraph G) {
        List<Node> Ct = greedy(G);
        List<Node> Vt = null;
        int k = 1;
        while (k<=kmax) {
            List<Node> Ctnew = VND(shake(Ct,k));
            if (Ctnew.size()>Ct.size()) {
                Ct = Ctnew;
            } else {
                k+=1;
            }
        }
        return Ct;
    }

    // Shaking: k random toppen uit CBC
    public List<Node> shake(List<Node> CBC,int k) {
        return CBC; //Moet nog geschreven worden
    }

    // Variable neighborhood descent: local search for optimum at every step
    public List<Node> VND(List<Node> CBC) {
        return CBC; //Moet nog geschreven worden
    }
}
