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
            // hier kan nog simplicial vertex van size 1 worden toegevoegd
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

    //interchangeStep
    public static void interchangeStep(UndirectedGraph G, List<Node> Ct, List<Node> Tt, Map<Node, Integer> phi) {
        boolean swappingPossible = true;
        while (swappingPossible) {
            // eerst Kt construeren
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
            // elementen van Kt proberen uitwisselen
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


    public static void main2(String[] args){
        UndirectedGraph graph2 = new UndirectedGraph();
        Node    a = graph2.addNode("1"),
                b = graph2.addNode("2"),
                c = graph2.addNode("3"),
                d = graph2.addNode("4"),
                e = graph2.addNode("5"),
                f = graph2.addNode("6"),
                g = graph2.addNode("7"),
                h = graph2.addNode("8"),
                i = graph2.addNode("9"),
                j = graph2.addNode("10"),
                k = graph2.addNode("11"),
                l = graph2.addNode("12"),
                m = graph2.addNode("13"),
                n = graph2.addNode("14"),
                o = graph2.addNode("15");
        graph2.addEdge(a, b);
        graph2.addEdge(a, c);
        graph2.addEdge(a, d);
        graph2.addEdge(a, e);
        graph2.addEdge(b, c);
        graph2.addEdge(b, d);
        graph2.addEdge(b, e);
        graph2.addEdge(c, d);
        graph2.addEdge(c, e);
        graph2.addEdge(d, e);
        graph2.addEdge(d, f);
        graph2.addEdge(d, g);
        graph2.addEdge(e, f);
        graph2.addEdge(e, g);
        graph2.addEdge(f, g);
        graph2.addEdge(f, h);
        graph2.addEdge(f, i);
        graph2.addEdge(f, j);
        graph2.addEdge(f, k);
        graph2.addEdge(g, l);
        graph2.addEdge(g, m);
        graph2.addEdge(g, n);
        graph2.addEdge(g, o);
        long startTime = System.currentTimeMillis();
        List<Node> best = VNS(graph2);
        System.out.println(best  + ": " + String.valueOf(System.currentTimeMillis()-startTime));
    }

    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of("brock200_2","brock200_4","brock400_2","brock400_4","brock800_2","brock800_4","C125.9","C250.9","C500.9","C1000.9",/*"C2000.5","C2000.9","C4000.5",*/"DSJC500_5","DSJC1000_5","gen200_p0.9_44","gen200_p0.9_55","gen400_p0.9_55","gen400_p0.9_65","gen400_p0.9_75","hamming8-4","hamming10-4","keller4","keller5","keller6","MANN_a27","MANN_a45","MANN_a81","p_hat300-1","p_hat300-2","p_hat300-3","p_hat700-1","p_hat700-2","p_hat700-3","p_hat1500-1","p_hat1500-2","p_hat1500-3"));
        for (int i = 0; i < testFiles.size(); i++) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            List<Node> beste = VNS(graph);
            System.out.println(testFiles.get(i) + ": " + beste.size() + ": " + String.valueOf(System.currentTimeMillis() - startTime));
            Boolean correct = true;
            for (int j = 1; j<beste.size(); j++) {
                for (int k = 0; k<j; k++) {
                    if (!graph.containsEdge(beste.get(j),beste.get(k))) {
                        correct = false;
                    }
                }
            }
            System.out.println(correct);
        }
    }

}