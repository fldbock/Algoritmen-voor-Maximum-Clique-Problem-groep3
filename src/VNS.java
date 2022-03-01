import graphlib.edges.Edge;
import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

public class VNS {

    private int kmax = 5;

    // VNS: variable neighborhood search
    public List<Node> VNS (UndirectedGraph G) {
        // CBC: current best clique
        // Alle toppen worden opgedeeld in 3 verzamelingen:
        // Ct: huidige kliek
        // Vt: nog in te delen toppen
        // Tt: huidige transversal
        // Ect: complement van de edges in Gt, Gt is de deelgraaf geïnduceerd door de toppen in Vt
        List<Node> CBC = new ArrayList<>();
        List<Node> Ct = new ArrayList<>();
        List<Node> Vt = G.getAllNodes();
        List<Node> Tt = new ArrayList<>();
        List<Edge> Ect = new ArrayList<>();
        for (Node node1: G.getAllNodes()) {
            for (Node node2: G.getAllNodes()) {
                if (node1 != node2) {
                    if (!G.containsEdge(node1,node2)) {
                        Edge edge = new Edge(node1,node2) {
                            @Override
                            public boolean isDirected() {
                                return false;
                            }
                        };
                        Ect.add(edge);
                    }
                }
            }
        }
        VND(Ct, Vt, Tt, Ect);
        int k = 1;
        while (k<=kmax) {
            shake(Ct, Vt, Tt, Ect, k);
            VND(Ct, Vt, Tt, Ect);
            if (Ct.size()>CBC.size()) {
                CBC = Ct;
            } else {
                k+=1;
            }
        }
        return CBC;
    }

    // Shaking: k random toppen van Ct naar Vt verplaatsen
    public void shake(List<Node> Ct, List<Node> Vt, List<Node> Tt, List<Edge> Ect, int k) {
        if (k <= Ct.size()) {
            Ct = null;
        } else {
            Collections.shuffle(Ct);
            for (int i = 0; i < k; i++) {
                Vt.add(Ct.get(i));
                Ct.remove(Ct.get(i));
            }
        }
    }

    // Variable neighborhood descent: local search met Ct als startkliek
    public void VND(List<Node> Ct, List<Node> Vt, List<Node> Tt, List<Edge> Ect) {
        //Moet nog geschreven worden
        SVT(Ct, Vt, Tt, Ect);
        while (!Vt.isEmpty()) {
            greedy2(Ct, Vt, Tt, Ect);
            SVT(Ct, Vt, Tt, Ect);
        }
        List<Node> Kt = new ArrayList<>();
        while (!Kt.isEmpty()) {
            interchangeStep(Ct, Vt, Tt, Ect, Kt);
        }
    }

    //SVT: simplicial vertex test
    public void SVT(List<Node> Ct, List<Node> Vt, List<Node> Tt, List<Edge> Ect) {
        //MOET NOG GESCHREVEN WORDEN
    }

    //Greedy approach that selects node with biggest degree
    public void greedy2(List<Node> Ct, List<Node> Vt, List<Node> Tt, List<Edge> Ect) {
        // We mappen elke top in Vt op zijn graad in Gct
        Map<Node, Integer> degree = new HashMap<>();
        for (Node node: Vt) {
            degree.put(node,0);
        }
        for (Edge edge: Ect) {
            degree.put(edge.getNode1(),degree.get(edge.getNode1())+1);
            degree.put(edge.getNode2(),degree.get(edge.getNode2())+1);
        }
        Node popularNode = Collections.min(degree.entrySet(), Map.Entry.comparingByValue()).getKey();
        Ct.add(popularNode);
        for (Edge edge: Ect) {
            if (edge.contains(popularNode)) {
                Ect.remove(edge);
                for (Node neighbor: edge.getNodes()) {
                    if (neighbor != popularNode) {
                        Tt.add(neighbor);
                        for (Edge edge2: Ect) {
                            if (edge2.contains(neighbor)) {
                                Ect.remove(edge2);
                            }
                        }
                        Vt.remove(neighbor);
                    }
                }
            }
        }
        Vt.remove(popularNode);
    }

    //interchangeStep
    public void interchangeStep(List<Node> Ct, List<Node> Vt, List<Node> Tt, List<Edge> Ect, List<Node> Kt) {
        //MOET NOG GESCHREVEN WORDEN
    }

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
}
