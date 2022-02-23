import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;


public class GreedySequential {
    public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for(int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            UndirectedGraph clique = bestInNew(graph);
            System.out.println(testFiles.get(i) + ": " + clique.numNodes() + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }

    //Heuristic that at each step adds the node with the most available connections
    public static UndirectedGraph bestInNew(UndirectedGraph graph){
        List<Node> clique = new ArrayList<>();

        //Sequential process
        while(graph.getAllNodes().size() != clique.size()){
            //Map nodes to number of connections
            Map<Node, Integer> connections = new HashMap<>();
            for(Node node: graph.getAllNodes()){
                if(!clique.contains(node)){
                    connections.put(node, graph.getDegree(node));
                }
            }
            //Choose and add node with the most available connections
            Node popularNode = Collections.max(connections.entrySet(), Map.Entry.comparingByValue()).getKey();
            clique.add(popularNode);
            for(Node node: graph.getAllNodes()){
                if(!graph.containsEdge(popularNode, node) && !node.equals(popularNode)){
                    graph.removeNode(node);
                }
            }
        }
        return graph;
    }
}
