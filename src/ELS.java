//import graphlib.*;
import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;

public class ELS {
    private ArrayList<Node> ccprev;
    private ArrayList<Node> cc;
    private ArrayList<Node> ccbest;
    private ArrayList<Node> p;
    private ArrayList<Node> om;
    private HashMap<Node,Integer> pa;
    private ArrayList<Node> intersectionpap;
    private ArrayList<Node> intersectionccp;



    public void effectiveLocalSearch(UndirectedGraph graph, ArrayList<Node> cc) {
        int gmax=-1;
        while (gmax!=0) {
            ccprev=cc;
            ArrayList<Node> d = new ArrayList<>(ccprev);
            p = new ArrayList<>(graph.getAllNodes());
            int g = 0;
            gmax=0;
            while (d.size()!=0){
                intersectionpap = intersection(pa,p);
                if (intersectionpap.size()!=0) { //Add-phase
                    add();
                    g++;
                    if (g>gmax) {
                        gmax=g;
                        ccbest=cc;
                    }
                } else { //drop-phase
                    drop();
                }
            }
        }
    }

    private ArrayList<Node> intersection(ArrayList<Node> list1, ArrayList<Node> list2) {
        ArrayList<Node> intersection = new ArrayList<>();
        for (Node node1: list1) {
            if (list2.contains(node1)) {
                intersection.add(node1);
            }
        }
        return intersection;
    }

    private ArrayList<Node> intersection(HashMap<Node, Integer> list1, ArrayList<Node> list2) {
        ArrayList<Node> intersection = new ArrayList<>();
        for (Node node1: list1.keySet()) {
            if (list2.contains(node1)) {
                intersection.add(node1);
            }
        }
        return intersection;
    }

    private void add(){
        Node v = null;
        int i = 0;
        for (Node node: intersectionpap) {
            if (pa.get(node)>i) {
                i = pa.get(node);
                v = node;
            }
        }
        cc.add(v);
        p.remove(v);
    }
    private void drop() {

    }

}
