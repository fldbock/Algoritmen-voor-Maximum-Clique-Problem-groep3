import graphlib.graphs.UndirectedGraph;
import graphlib.edges.UndirectedEdge;
import graphlib.nodes.Node;
import graphlib.edges.Edge;

import java.io.*;
import java.net.URL;
import java.util.*;

public class EffectiveLocalSearch {

    public void effectiveLocalSearch(UndirectedGraph graph) {
        ArrayList<Node> cc = new ArrayList<>(); // cc = current clique
        ArrayList<Node> d = new ArrayList<>(); // d = stopping criterium
        List<Node> nodes = graph.getAllNodes(); // nodes = all nodes
        ArrayList<Node> rest = new ArrayList<>(); // rest = {nodes we can still try}
        Map<Node, Integer> pa = new HashMap<>(); // pa = potential additions
        ArrayList<Node> intersection = new ArrayList<>(); // intersection = intersection of the vertices in pa and rest
        ArrayList<Node> om = new ArrayList<>(); // om = one edge missing = vertices that are connected to |cc| - 1 vertices of cc
        ArrayList<Node> best = new ArrayList<>; // best = best clique to now on
        int g = 0;
        int gmax = 0;


        //We build a beginning clique and initialise pa ### MOET DIT VOOR ELKE v ?
        int i = 0;
        Node v = null;
        for (Node node: nodes) {
            int j = graph.getDegree(node);
            pa.put(node, j);
            if (j > i) {
                v = node;
                i=j;
            }
        }
        cc.add(v);
        d.add(v);
        //update pa, intersection and om
        for (Node node: pa.keySet()) {
            if (graph.containsEdge(node, v)) {
                pa.replace(node, pa.get(node) - 1);
                intersection.add(node);
            } else {
                pa.remove(node);
                om.add(node);
            }
        }


        // RECURSIE?
        while (d.size()!=0) {
            if (intersection.size()!=0) { //Add-phase
                v = null;
                i=0;
                for (Node node: intersection) {
                    int j = pa.get(node);
                    pa.put(node, j);
                    if (j > i) {
                        v = node;
                        i=j;
                    }
                }
                cc.add(v);
                g++;
                rest.remove(v);
                if (g>gmax) {
                    best = cc;
                    gmax = g;
                }
            } else { //Drop-phase



        }
    }

    public void recursiveEffectiveLocalSearch(UndirectedGraph graph, ArrayList<Node> nodes, ArrayList<Node> cc, HashMap<Node,Integer> pa, ArrayList<Node> om, ArrayList<Node> best, ) {
        ArrayList<Node> prevCC = cc;
        ArrayList<Node> rest = cc;
        List<Node> nodes = graph.getAllNodes();
        Map<Node, Integer> potentialAdds = new HashMap<Node, Integer>();
        for (Node node: nodes){
            potentialAdds.put(node, graph.getDegree(node));
        }
        if (prevCC.size()==0){

        }
        int g = 0;
        int gmax = 0;
        while (gmax==0) {
            gmax++;
        }
    }
}
