import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;

public class ELS {
    private static UndirectedGraph graph;
    private static ArrayList<Node> nodes;
    private static ArrayList<Node> cc;
    private static ArrayList<Node> ccbest;
    private static ArrayList<Node> p;
    private static ArrayList<Node> om;
    private static HashMap<Node, Integer> pa;
    private static ArrayList<Node> intersectionpap;
    private static ArrayList<Node> intersectionccp;

    public static void main(String[] args) {
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of("C125.9", "C250.9", "DSJC1000_5", "DSJC500_5", "C2000.5", "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (String testFile : testFiles) {
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFile);
            long startTime = System.currentTimeMillis();
            int numNodes = effectiveLocalSearch(graph, true);
            System.out.println(testFile + " naive" + ": " + numNodes + ": " + String.valueOf(System.currentTimeMillis() - startTime));
            startTime = System.currentTimeMillis();
            numNodes = effectiveLocalSearch(graph, false);
            System.out.println(testFile + " greedy" + ": " + numNodes + ": " + String.valueOf(System.currentTimeMillis() - startTime));
        }
    }

    public static int effectiveLocalSearch(UndirectedGraph graph1, boolean naive) {
        graph = graph1;
        nodes = new ArrayList<>(graph.getAllNodes());
        generateClique(naive);
        generatePAandOM();

        int gmax = -1;
        while (gmax != 0) {
            ArrayList<Node> ccprev = new ArrayList(cc);
            ArrayList<Node> d = new ArrayList<>(cc);
            p = new ArrayList<>(graph.getAllNodes());
            int g = 0;
            gmax = 0;
            while (d.size() != 0) {
                intersectionpap = intersection(pa.keySet(), p);
                intersectionccp = intersection(cc, p);
                Node v = null;
                if (intersectionpap.size() != 0) { //Add-phase
                    v = add();
                    g++;
                    if (g > gmax) {
                        gmax = g;
                        ccbest = new ArrayList<>(cc);
                    }
                } else { //drop-phase
                    v = drop();
                    g--;
                    if (ccprev.contains(v)) {
                        d.remove(v);
                    }
                }
                update(v);
            }
            if (gmax > 0) {
                cc = new ArrayList<>(ccbest);
            } else {
                cc = new ArrayList<>(ccprev);
            }
            generatePAandOM();
        }
        return cc.size();
    }

    private static ArrayList<Node> intersection(Collection<Node> collection1, Collection<Node> collection2) {
        ArrayList<Node> intersection = new ArrayList<>();
        for (Node node1 : collection1) {
            if (collection2.contains(node1)) {
                intersection.add(node1);
            }
        }
        return intersection;
    }

    private static Node add() {
        Node v = null;
        int i = 0;
        for (Node node : intersectionpap) {
            if (pa.get(node) >= i) {
                i = pa.get(node);
                v = node;
            }
        }
        cc.add(v);
        p.remove(v);
        return v;
    }

    private static Node drop() {
        Node v = null;
        int i = 0;
        for (Node node : intersectionccp) {
            int j = 0;
            for (Node node1 : om) {
                if (!graph.containsEdge(node, node1)) {
                    j++;
                }
            }
            if (j >= i) {
                i = j;
                v = node;
            }
        }
        cc.remove(v);
        p.remove(v);
        return v;
    }

    private static void update(Node v) {
        //update pa en update om
        if (cc.contains(v)) {// Node v just got added
            pa.remove(v);
            for (int i = 0; i < om.size(); i++) {
                if (!graph.containsEdge(om.get(i), v)) {
                    om.remove(i);
                    i--;
                }
            }
            ArrayList<Node> copyPA = new ArrayList<>(pa.keySet());
            for (Node node : copyPA) {
                if (graph.containsEdge(v, node)) {
                    pa.replace(node, pa.get(node) - 1);
                } else {
                    pa.remove(node);
                    om.add(node);
                }
            }
        } else { //Node v just got removed
            for (int i = 0; i < om.size(); i++) {
                Node u = om.get(i);
                if (!graph.containsEdge(u, v)) {
                    int j = 0;
                    for (Node node : pa.keySet()) {
                        if (graph.containsEdge(node, u)) {
                            j++;
                        }
                    }
                    pa.put(u, j);
                    om.remove(u);
                    i--;
                }
            }
            for (Node node : graph.getAllNodes()) {
                if (!pa.containsKey(node) & !om.contains(node) & !cc.contains(node)) {
                    int j = 0;
                    for (Node node1 : cc) {
                        if (graph.containsEdge(node, node1)) {
                            j++;
                        }
                    }
                    if (j == cc.size() - 1) {
                        om.add(node);
                    }
                }
            }
        }
    }

    private static void generatePAandOM() {
        om = new ArrayList<>();
        pa = new HashMap<>();
        ArrayList<Node> optionsPA = new ArrayList<>();
        for (Node node : nodes) {
            if (!cc.contains(node)) {
                int i = 0; // Number of nodes in cc that is not adjacent with node
                int j = 0; // index for iteration
                while (i < 2 & j < cc.size()) {
                    if (!graph.containsEdge(node, cc.get(j))) {
                        i++;
                    }
                    j++;
                }
                if (i == 0) {
                    optionsPA.add(node);
                } else if (i == 1) {
                    om.add(node);
                }
            }
        }
        for (Node node1 : optionsPA) {
            int k = 0;
            for (Node node2 : optionsPA) {
                if (graph.containsEdge(node1, node2)) {
                    k++;
                }
            }
            pa.put(node1, k);
        }
    }

    private static void generateClique(boolean naive) {
        if (naive) {
            cc = new ArrayList<Node>();
            Node v = nodes.get(0);
            cc.add(v);
        } else {
            GreedySequential gs = new GreedySequential();
            cc=new ArrayList<Node>(gs.bestInNew(graph).getAllNodes());
        }
        ccbest = new ArrayList<>(cc);
    }
}