import graphlib.edges.Edge;
import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

public class VNS {

    private int kmax = 5;

    // VNS: variable neighborhood search
    public static List<Node> VNS (UndirectedGraph G) {
        // CBC: current best clique
        // Alle toppen worden opgedeeld in 3 verzamelingen:
        // Ct: huidige kliek
        // Vt: nog in te delen toppen
        // Tt: huidige transversal
        // Gct: complement van deelgraaf van G voortgebracht door Vt
        // phi: houdt bij hoeveel toppen in Ct adjacent zijn aan elke top
        List<Node> CBC = new ArrayList<>();
        List<Node> Ct = new ArrayList<>();
        List<Node> Vt = G.getAllNodes();
        List<Node> Tt = new ArrayList<>();
        UndirectedGraph Gct = new UndirectedGraph();
        Map<Node, Integer> phi = new HashMap<>();
        for (int i = 0; i<G.getAllNodes().size(); i++) {
            Node node1 = G.getAllNodes().get(i);
            Gct.addNode(node1);
            phi.put(node1,0);
            for (int j = 0; j<i; j++) {
                Node node2 = G.getAllNodes().get(j);
                if (!G.containsEdge(node1,node2)) {
                    Gct.addEdge(node1,node2);
                }
            }
        }
        VND(G, Ct, Vt, Tt, Gct, phi);
        CBC = List.copyOf(Ct);
        int k = 1;
        while (k<Ct.size()) {
            shake(G, Ct, Vt, Gct, phi, k);
            VND(G, Ct, Vt, Tt, Gct, phi);
            if (Ct.size()>CBC.size()) {
                CBC = List.copyOf(Ct);
            } else {
                k+=1;
            }
        }
        return CBC;
    }

    // Shaking: k random toppen van Ct naar Vt verplaatsen
    public static void shake(UndirectedGraph G, List<Node> Ct, List<Node> Vt, UndirectedGraph Gct, Map<Node, Integer> phi, int k) {
        Collections.shuffle(Ct);
        for (int i = 0; i<k; i++) {
            Node transferNode = Ct.get(Ct.size()-1);
            Gct.addNode(transferNode);
            Vt.add(transferNode);
            for (Node neighbor: Gct.getAllNodes()) {
                if (!G.containsEdge(transferNode,neighbor) && !neighbor.equals(transferNode)) {
                    Gct.addEdge(transferNode,neighbor);
                }
            }
            Ct.remove(transferNode);
            for (Node neighbor: G.getNeighbours(transferNode)) {
                phi.put(neighbor,phi.get(neighbor)-1);
            }
        }
    }

    // Variable neighborhood descent: local search met Ct als startkliek
    public static void VND(UndirectedGraph G, List<Node> Ct, List<Node> Vt, List<Node> Tt, UndirectedGraph Gct, Map<Node, Integer> phi) {
        while (!Vt.isEmpty()) {
            greedy2(G, Ct, Vt, Tt, Gct, phi);
        }
        interchangeStep(G, Ct, Tt, phi);
    }

    //Greedy approach that selects node with biggest degree, or smallest degree in the complement
    public static void greedy2(UndirectedGraph G, List<Node> Ct, List<Node> Vt, List<Node> Tt, UndirectedGraph Gct, Map<Node, Integer> phi) {
        Map<Node, Integer> degree2 = new HashMap<>();
        for (Node node: Gct.getAllNodes()) {
            degree2.put(node, Gct.getDegree(node));
        }
        Node popularNode2 = Collections.min(degree2.entrySet(), Map.Entry.comparingByValue()).getKey();
        Ct.add(popularNode2);
        for (Node neighbor: G.getNeighbours(popularNode2)) {
            phi.put(neighbor,phi.get(neighbor)+1);
        }
        for (Node neighbor: Gct.getNeighbours(popularNode2)) {
            Tt.add(neighbor);
            Vt.remove(neighbor);
            Gct.removeNode(neighbor);
        }
        Vt.remove(popularNode2);
        Gct.removeNode(popularNode2);
    }

    //interchangeStep
    public static void interchangeStep(UndirectedGraph G, List<Node> Ct, List<Node> Tt, Map<Node, Integer> phi) {
        // eerst Kt construeren
        List<Node> Kt = new ArrayList<>();
        for (Node node: Tt) {
            if (phi.get(node) == Ct.size()-1) {
                Kt.add(node);
            }
        }
        // elementen van Kt proberen uitwisselen
        Collections.shuffle(Kt);
        Node swap = null;
        if (!Kt.isEmpty()) {
            Node node = Kt.get(0);
            for (Node possibleSwap: Ct) {
                if (!G.containsEdge(node,possibleSwap)) {
                    swap = possibleSwap;
                }
            }
            for (Node node2: Kt) {
                if (!G.containsEdge(node2,swap) && G.containsEdge(node,node2)) {
                    Ct.add(node);
                    Tt.remove(node);
                    for (Node neighbor: G.getNeighbours(node)) {
                        phi.put(neighbor,phi.get(neighbor)+1);
                    }
                    Ct.add(node2);
                    Tt.remove(node2);
                    for (Node neighbor: G.getNeighbours(node2)) {
                        phi.put(neighbor,phi.get(neighbor)+1);
                    }
                    Ct.remove(swap);
                    Tt.add(swap);
                    for (Node neighbor: G.getNeighbours(swap)) {
                        phi.put(neighbor,phi.get(neighbor)-1);
                    }


                }
            }
        }
    }


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
        graph2.addEdge(a, b);
        graph2.addEdge(a, d);
        graph2.addEdge(a, e);
        graph2.addEdge(b, c);
        graph2.addEdge(b, d);
        graph2.addEdge(b, f);
        graph2.addEdge(b, g);
        graph2.addEdge(c, d);
        graph2.addEdge(c, g);
        graph2.addEdge(c, h);
        graph2.addEdge(d, e);
        graph2.addEdge(d, f);
        graph2.addEdge(d, g);
        graph2.addEdge(d, h);
        graph2.addEdge(e, f);
        graph2.addEdge(f, g);
        graph2.addEdge(g, h);
        graph2.addEdge(f, h);
        long startTime = System.currentTimeMillis();
        List<Node> best = VNS(graph2);
        System.out.println(best.size()  + ": " + String.valueOf(System.currentTimeMillis()-startTime));
    }

}