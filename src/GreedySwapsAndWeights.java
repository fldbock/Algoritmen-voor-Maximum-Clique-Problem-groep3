import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;


public class GreedySwapsAndWeights {
    public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            List<Node> clique = procedureDAGS(graph);
            System.out.println(testFiles.get(i) + ": " + clique.size() + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }


    public static List<Node> procedureDAGS(UndirectedGraph graph){
        int MAX_ITER = Math.round(graph.getAllNodes().size() /8);
        int MAX_WEIGHT = 3;
        int START_SWAP = 5;
        int MAX_CURRENT_SWAPS = 5;
        double DELTA = 0.1;
        List<Node> clique;
        List<Node> bestClique = new ArrayList<>();
        Map<Node, Integer> countInCliques = new HashMap<>();
        for (Node node: graph.getAllNodes()){
            countInCliques.put(node, 0);
        }
        //run greedySwap for every start node, keep count of how often each node is used in a final clique.
        for (Node node:graph.getAllNodes()){
            clique = greedySwapCliquer(graph, node, START_SWAP, MAX_CURRENT_SWAPS);
            for (Node cliqueNode: clique){
                countInCliques.put(node, countInCliques.get(node)+1);
            }
            if (clique.size() > bestClique.size()){
                bestClique = new ArrayList<>(clique);
            }
        }
        //Apply greedy weighted for these nodes.
        Map<Node, Double> nodeWeights = new HashMap<>();
        for (int i = 0; i < DELTA * graph.getAllNodes().size(); i++){
            Node node = Collections.max(countInCliques.entrySet(), Map.Entry.comparingByValue()).getKey();
            countInCliques.remove(node);
            for (Node graphNode: graph.getAllNodes()){
                nodeWeights.put(graphNode, 1.0);
            }
            for (int t = 1; t < MAX_ITER; t++) {
                clique = greedyWeightCliquer(graph, node, nodeWeights);
                if (clique.size() > bestClique.size()){
                    bestClique = new ArrayList<>(clique);
                }
                weightUpdater(clique, nodeWeights, MAX_WEIGHT);
            }
        }
        return bestClique;
    }


    public static List<Node> greedyWeightCliquer(UndirectedGraph graph, Node startnode, Map<Node, Double> nodeWeightsOG){
        //initialisation: copy graph and remove every node not connected to start node.
        Map<Node, Double> nodeWeights = new HashMap<>(nodeWeightsOG);
        nodeWeights.remove(startnode);
        List<Node> clique = new ArrayList<>();
        clique.add(startnode);
        Set<Node> keySetCopy = new HashSet<>(nodeWeights.keySet());
        for(Node node: keySetCopy){
            if(!graph.containsEdge(startnode, node) && !node.equals(startnode)){
                nodeWeights.remove(node);
            }
        }
        while(nodeWeights.size() != 0){
            //Choose and add node with the highest weight and remove it from options.
            Node popularNode = Collections.max(nodeWeights.entrySet(), Map.Entry.comparingByValue()).getKey();
            nodeWeights.remove(popularNode);
            clique.add(popularNode);
            //update graph to only keep nodes attached to the popular node.
            keySetCopy = new HashSet<>(nodeWeights.keySet());
            for(Node node: keySetCopy){
                if(!graph.containsEdge(popularNode, node) && !node.equals(popularNode)){
                    nodeWeights.remove(node);
                }
            }
        }
        return clique;
    }


    //Heuristic that at each step adds the node with the most available connections or swaps 1 node to a potentially better one.
    public static List<Node> greedySwapCliquer(UndirectedGraph graph, Node startnode, int START_SWAP, int MAX_CURRENT_SWAPS){
        //initialisation of variables used in loop
        List<Node> clique = new ArrayList<>();
        clique.add(startnode);
        Map<Node, Integer> neighbourDegree = new HashMap<>();
        for (Node node:graph.getNeighbours(startnode)){
            neighbourDegree.put(node, 1);
        }
        int currentSwaps = 0;
        int totalSwaps = 0;
        Node lastSwapNode = null;
        Node removalNode = null;

        //clique building loop
        while(clique.size() == Collections.max(neighbourDegree.values())){
            //Build cZero and cOne
            List<Node> cZero = new ArrayList<>();
            List<Node> cOne = new ArrayList<>();
            for (Node node: neighbourDegree.keySet()){
                if (neighbourDegree.get(node) ==  clique.size()){
                    cZero.add(node);
                } else if (neighbourDegree.get(node) == clique.size()-1){
                    cOne.add(node);
                }
            }

            //find popular node to swap or add
            int popularDegree = -1;
            Node popularNode = null;
            boolean swap = false;
            for (Node node: cZero){
                if (graph.getDegree(node) > popularDegree){
                    popularDegree = graph.getDegree(node);
                    popularNode = node;
                }
            }
            if(totalSwaps >= START_SWAP & currentSwaps < MAX_CURRENT_SWAPS){
                for (Node node: cOne){
                    if (graph.getDegree(node) - 1 > popularDegree & !(node==lastSwapNode)){
                        popularDegree = graph.getDegree(node) - 1;
                        popularNode = node;
                        swap = true;
                    }
                }
            }

            //remove a node if swap is true.
            if(swap) {
                for (Node node : clique) {
                    if (!graph.getNeighbours(popularNode).contains(node)) {
                        removalNode = node;
                    }
                }
                clique.remove(removalNode);
                neighbourDegreeRemover(graph, removalNode, clique, neighbourDegree);
                lastSwapNode = removalNode;
                currentSwaps++;
                totalSwaps++;
            } else {
                currentSwaps = 0;
            }

            //add a node
            clique.add(popularNode);
            neighbourDegree.remove(popularNode);
            neighbourDegreeAdder(graph, popularNode, clique, neighbourDegree);
        }

        return clique;
    }

    //Updates the amount of connections to the clique for every neighbour of the clique.
    private static void neighbourDegreeAdder(UndirectedGraph graph, Node node, List<Node> clique, Map<Node, Integer> neighbourDegree){
        for (Node neighbour:graph.getNeighbours(node)) {
            if (!clique.contains(neighbour)){
                if (neighbourDegree.keySet().contains(neighbour)){
                    neighbourDegree.put(neighbour, neighbourDegree.get(neighbour) + 1);
                } else {
                    neighbourDegree.put(node, 1);
                }
            }
        }
    }

    //updates the amount of connections when removalNode was removed from clique
    private static void neighbourDegreeRemover(UndirectedGraph graph, Node removalNode, List<Node> clique, Map<Node, Integer> neighbourDegree){
        Set<Node> keySetCopy = new HashSet<>(neighbourDegree.keySet());
        for (Node node: keySetCopy){
            if (graph.getNeighbours(node).contains(removalNode)){
                if (neighbourDegree.get(node) > 1){
                    neighbourDegree.put(node, neighbourDegree.get(node) - 1);
                } else {
                    neighbourDegree.remove(node);
                }
            }
        }
        neighbourDegree.put(removalNode, clique.size());
    }

    //updates weights based on nodes used in clique.
    private static void weightUpdater(List<Node> clique, Map<Node, Double> nodeWeights, double MAX_WEIGHT){
        for (Node node: clique){
            if (nodeWeights.get(node)> 1/Math.pow(2.0,MAX_WEIGHT-1)){
                nodeWeights.put(node, nodeWeights.get(node)/2);
            } else {
                nodeWeights.put(node, 0.0);
            }
        }
    }

//    builds a graph class clique from a list of Nodes
//    public static UndirectedGraph cliqueFromNodes(List<Node> clique){
//        UndirectedGraph graph = new UndirectedGraph();
//        for (Node node:clique){
//            graph.addNode(node);
//            for (Node neighbour:graph.getAllNodes()){
//                if (neighbour != node){
//                    graph.addEdge(node, neighbour);
//                }
//            }
//        }
//        return graph;
//    }


}
