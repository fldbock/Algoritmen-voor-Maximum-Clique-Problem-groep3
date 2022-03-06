import graphlib.edges.Edge;
import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

public class VNS {

    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of("C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5", "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int beste = VNS(graph).size();
            System.out.println(testFiles.get(i) + ": " + beste + ": " + String.valueOf(System.currentTimeMillis() - startTime));
        }
    }

    private int kmax = 5;

    // VNS: variable neighborhood search
    public static List<Node> VNS (UndirectedGraph G) {
        // CBC: current best clique
        // Bij elke iteratie worden alle toppen opgedeeld in 3 verzamelingen
        // Ct: huidige kliek
        // Vt: huidge verzameling nog in te delen toppen
        // Tt: huidige transversal
        // Gct: complement van deelgraaf van G voortgebracht door Vt
        // phi: hashmap die voor elke top van G het aantal adjacente toppen in Ct weergeeft
        List<Node> CBC = new ArrayList<>();
        List<Node> Ct = new ArrayList<>();
        List<Node> Vt = G.getAllNodes();
        List<Node> Tt = new ArrayList<>();
        // we creëren initiële Gct
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
        // we zoeken een initieel lokaal optimum
        VND(G, Ct, Vt, Tt, Gct, phi);
        CBC = List.copyOf(Ct);
        int k = 1;
        while (k<4 && k<Ct.size()) {
            shake(G, Ct, Vt, Tt, Gct, phi, k);
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
    public static void shake(UndirectedGraph G, List<Node> Ct, List<Node> Vt, List<Node> Tt, UndirectedGraph Gct, Map<Node, Integer> phi, int k) {
        Collections.shuffle(Ct);
        for (int i = 0; i<k; i++) {
            Node transferNode = Ct.get(Ct.size()-1);
            Gct.addNode(transferNode);
            Vt.add(transferNode);
            Ct.remove(transferNode);
            for (Node neighbor: G.getNeighbours(transferNode)) {
                phi.put(neighbor,phi.get(neighbor)-1);
            }
        }
        for (int i = 0; i < Tt.size(); i ++) {
            Node node = Tt.get(i);
            if (phi.get(node) == Ct.size()) {
                Tt.remove(node);
                Vt.add(node);
                Gct.addNode(node);
                for (Node neighbor: Gct.getAllNodes()) {
                    if (!G.containsEdge(node,neighbor) && !neighbor.equals(node)) {
                        Gct.addEdge(node,neighbor);
                    }
                }
                i --;
            }
        }
    }

    // Variable neighborhood descent: local search met Ct als startkliek
    public static void VND(UndirectedGraph G, List<Node> Ct, List<Node> Vt, List<Node> Tt, UndirectedGraph Gct, Map<Node, Integer> phi) {

        Map<Integer,List<Node>> degrees = new TreeMap<>();
        for (Node node: Vt) {
            int degree = Gct.getDegree(node);
            if (!degrees.containsKey(degree)) {
                degrees.put(degree, new ArrayList<Node>());
            }
            degrees.get(degree).add(node);
        }
        while (!Vt.isEmpty()) {
            //SVT test
            if (degrees.containsKey(0)) {
                for (Node node: degrees.get(0)) {
                    Ct.add(node);
                    Vt.remove(node);
                    Gct.removeNode(node);
                    for (Node neighbor: G.getNeighbours(node)) {
                        phi.put(neighbor,phi.get(neighbor)+1);
                    }
                }
                degrees.remove(0);
            }
            // hier kunnen nog simplicial vertices van size 1 worden toegevoegd
            //Greedy: finding minimum degree
            if (!degrees.isEmpty()) {
                int minDegree = ((TreeMap<Integer, List<Node>>) degrees).firstKey();
                List<Node> possibleAdd= degrees.get(minDegree);
                Node node;
                if (possibleAdd.size() == 1) {
                    node = possibleAdd.get(0);
                    degrees.remove(minDegree);
                } else {
                    Collections.shuffle(possibleAdd);
                    node = possibleAdd.get(0);
                    degrees.get(minDegree).remove(node);
                }
                if (Vt.contains(node)) {
                    Ct.add(node);
                    for (Node neighbor : G.getNeighbours(node)) {
                        phi.put(neighbor, phi.get(neighbor) + 1);
                    }
                    for (Node neighbor : Gct.getNeighbours(node)) {
                        Tt.add(neighbor);
                        Vt.remove(neighbor);
                        Gct.removeNode(neighbor);
                    }
                    Vt.remove(node);
                    Gct.removeNode(node);
                }
            }
        }
        interchangeStep(G, Ct, Tt, phi);
    }

    // interchangeStep: kijkt of er door één top uit de huidige kliek te verwijderen, meerdere nieuwe toppen kunnen toegevoegd worden
    public static void interchangeStep(UndirectedGraph G, List<Node> Ct, List<Node> Tt, Map<Node, Integer> phi) {
        boolean swappingPossible = true;
        while (swappingPossible) {
            // eerst Kt construeren, dit is de verzameling toppen in Tt die slechts aan één top van Ct niet adjacent zijn
            List<Node> Kt = new ArrayList<>();
            for (int i = 0; i < Tt.size(); i ++) {
                Node node = Tt.get(i);
                if (phi.get(node) == Ct.size()) {
                    Ct.add(node);
                    Tt.remove(node);
                    for (Node neighbor : G.getNeighbours(node)) {
                        phi.put(neighbor, phi.get(neighbor) + 1);
                    }
                    i --;
                } else if (phi.get(node) == Ct.size()-1) {
                    Kt.add(node);
                }
            }
            // elementen van Kt proberen uitwisselen met elementen van Ct
            Collections.shuffle(Kt);
            if (Kt.size() > 1) {
                boolean swapped = false;
                int i = 1;
                while (!swapped && i < Kt.size()) {
                    Node node = Kt.get(i);
                    Node swap = null;
                    boolean found = false;
                    int j = 0;
                    while (!found && j < Ct.size()) {
                        if (!G.containsEdge(node, Ct.get(j))) {
                            swap = Ct.get(j);
                            found = true;
                        }
                        j ++;
                    }
                    int k =0;
                    while (!swapped && k < i) {
                        Node node2 = Kt.get(k);
                        if (!G.containsEdge(node2, swap) && G.containsEdge(node, node2)) {
                            Ct.add(node);
                            Tt.remove(node);
                            for (Node neighbor : G.getNeighbours(node)) {
                                phi.put(neighbor, phi.get(neighbor) + 1);
                            }
                            Ct.add(node2);
                            Tt.remove(node2);
                            for (Node neighbor : G.getNeighbours(node2)) {
                                phi.put(neighbor, phi.get(neighbor) + 1);
                            }
                            Ct.remove(swap);
                            Tt.add(swap);
                            for (Node neighbor : G.getNeighbours(swap)) {
                                phi.put(neighbor, phi.get(neighbor) - 1);
                            }
                            swapped = true;
                        }
                        k ++;
                    }
                    i ++;
                    if (i == Kt.size() && !swapped) {
                        swappingPossible = false;
                    }
                }
            } else {
                swappingPossible = false;
            }
        }
    }
}