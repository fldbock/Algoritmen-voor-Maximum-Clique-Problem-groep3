//import graphlib.*;
import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;

public class ELS {
    private static UndirectedGraph graph;
    private static ArrayList<Node> cc;
    private static ArrayList<Node> ccbest;
    private static ArrayList<Node> p;
    private static ArrayList<Node> om;
    private static HashMap<Node, Integer> pa;
    private static ArrayList<Node> intersectionpap;
    private static ArrayList<Node> intersectionccp;

  public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of("C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5", "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for(int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph1 = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            graph = graph1;
            cc=new ArrayList<>();
           //Hieronder stellen we een begingraaf op met 1 top, later proberen via greedy!
            ArrayList<Node> nodes = new ArrayList<>(graph1.getAllNodes());
            Node v = nodes.get(0);
            cc.add(v);
            ccbest= null;
            om = new ArrayList<>();
            ArrayList<Node> paopt = new ArrayList<>();
            pa = new HashMap<>();
            for (int j = 0; j < nodes.size(); j++) {
                if (graph1.containsEdge(v,nodes.get(i))) {
                    paopt.add(nodes.get(i));
                } else {
                    om.add(nodes.get(i));
                }
            }
            for (Node node1: paopt) {
                int k = 0;
                for (Node node2: paopt) {
                    if (graph1.containsEdge(node1,node2)) {
                        k++;
                    }
                }
                pa.put(node1,k);
            }
            int numNodes = effectiveLocalSearch();
            System.out.println(testFiles.get(i) + ": " + numNodes + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }



    public static int effectiveLocalSearch() {
        int gmax = -1;
        while (gmax != 0) {
            ArrayList<Node> ccprev = new ArrayList(cc);
            ArrayList<Node> d = new ArrayList<>(cc);
            p = new ArrayList<>(graph.getAllNodes());
            int g = 0;
            gmax = 0;
            while (d.size() != 0) {
                intersectionpap = intersection(pa, p);
                intersectionccp = intersection(cc,p);
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
                //intersectionccp = intersection(cc,p);
                //intersectionpap = intersection(pa,p);
            }
            if (gmax>0) {
                cc = new ArrayList<>(ccbest);
            } else {
                cc = new ArrayList<>(ccprev);
            }
        }
        return cc.size();
    }

    private static ArrayList<Node> intersection(List<Node> list1, List<Node> list2) {
        ArrayList<Node> intersection = new ArrayList<>();
        for (Node node1 : list1) {
            if (list2.contains(node1)) {
                intersection.add(node1);
            }
        }
        return intersection;
    }

    private static ArrayList<Node> intersection(HashMap<Node, Integer> list1, ArrayList<Node> list2) {
        ArrayList<Node> intersection = new ArrayList<>();
        for (Node node1 : list1.keySet()) {
            if (list2.contains(node1)) {
                intersection.add(node1);
            }
        }
        return intersection;
    }

    private static Node add() {
        Node v = null;
        int i = 0;
        for (Node node : intersectionpap) {
            if (pa.get(node) > i) {
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
            if (j > i) {
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
            for (Node node : pa.keySet()) {
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
                    for (Node node: pa.keySet()) {
                        if (graph.containsEdge(node,u)) {
                            j++;
                        }
                    }
                    pa.put(u,j);
                    om.remove(u);
                    i--;
                }
            }
            for (Node node: graph.getAllNodes()) {
                if (!pa.containsKey(node) & !om.contains(node) & !cc.contains(node)) {
                    int j = 0;
                    for (Node node1: cc) {
                        if (graph.containsEdge(node,node1)) {
                            j++;
                        }
                    }
                    if (j == cc.size()-1) {
                        om.add(node);
                    }
                }
            }
        }
    }
}
