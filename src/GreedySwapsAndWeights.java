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
        //Apply greedy weighted for starting in the least used nodes from above.
        Map<Node, Double> nodeWeights = new HashMap<>();
        for (int i = 0; i < DELTA * graph.getAllNodes().size(); i++){
            Node startNode = Collections.max(countInCliques.entrySet(), Map.Entry.comparingByValue()).getKey();
            countInCliques.remove(startNode);
            for (Node graphNode: graph.getAllNodes()){
                nodeWeights.put(graphNode, 1.0);
            }
            for (int t = 1; t < MAX_ITER; t++) {
                clique = greedyWeightCliquer(graph, startNode, nodeWeights);
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
        Map<Node, Integer> valueMapCZero = new HashMap<>();
        Map<Node, Integer> valueMapCOne = new HashMap<>();
        for (Node node:graph.getNeighbours(startnode)){
            neighbourDegree.put(node, 1);
            valueMapAdder(graph, node, valueMapCZero, valueMapCOne);
            valueMapCZero.put(node, adjacencyToCOneCounter(graph, node, valueMapCZero.keySet()));
        }
        int currentSwaps = 0;
        int totalSwaps = 0;
        Node lastSwapNode = null;
        Node removalNode = null;

        //clique building loop
        while(clique.size() == Collections.max(neighbourDegree.values())){
            //Find the best node to add or swap based on adjacency to the cZero node set.
            int bestNodeValue = -1;
            boolean swap = false;
            Node popularNode = null;
            for(Node node : valueMapCZero.keySet()){
                if (valueMapCZero.get(node)>bestNodeValue){
                    popularNode = node;
                    bestNodeValue = valueMapCZero.get(node);
                }
            }
            if(totalSwaps >= START_SWAP & currentSwaps < MAX_CURRENT_SWAPS) {
                for (Node node : valueMapCZero.keySet()){
                    if (valueMapCOne.get(node)>bestNodeValue & node != lastSwapNode){
                        popularNode = node;
                        bestNodeValue = valueMapCOne.get(node);
                        swap = true;
                    }
                }
            }
            //remove a node is swap is TRUE.
            if(swap) {
                for (Node node : clique) {
                    if (!graph.getNeighbours(popularNode).contains(node)) {
                        removalNode = node;
                    }
                }
                List <Node> cOneNewList = new ArrayList<>();
                for (Node node: neighbourDegree.keySet()){
                    if (neighbourDegree.get(node)==clique.size()-2){
                        cOneNewList.add(node);
                    }
                }
                clique.remove(removalNode);
                neighbourDegreeRemover(graph, removalNode, clique, neighbourDegree);
                for (Node node: neighbourDegree.keySet()){
                    if (neighbourDegree.get(node)==clique.size()-2 & cOneNewList.contains(node)){
                        cOneNewList.remove(node);
                    }
                }
                valueMapCZero.put(removalNode, valueMapCZero.size());
                for (Node node : valueMapCZero.keySet()){
                    valueMapCZero.put(node, valueMapCZero.get(node)+1);
                }
                for (Node node : valueMapCOne.keySet()){
                    if (neighbourDegree.get(node) == clique.size()){
                        valueMapAdder(graph, node, valueMapCZero, valueMapCOne);
                        valueMapCZero.put(node, valueMapCOne.get(node));
                        valueMapCOne.remove(node);
                    } else {
                        valueMapCOne.put(node, valueMapCOne.get(node)+1);
                    }
                }
                for (Node node: cOneNewList){
                    valueMapCOne.put(node, adjacencyToCOneCounter(graph, node, valueMapCZero.keySet()));
                }
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
            valueMapCZero.remove(popularNode);
            valueMapRemover(graph, popularNode, valueMapCZero, valueMapCOne);
            Set<Node> keySetCopy = new HashSet<>(valueMapCZero.keySet());
            for (Node node:keySetCopy){
                if (neighbourDegree.get(node) != clique.size()){
                    valueMapRemover(graph, node, valueMapCZero, valueMapCOne);
                    valueMapCOne.put(node, valueMapCZero.get(node));
                    valueMapCZero.remove(node);
                }
            }
            keySetCopy = new HashSet<>(valueMapCOne.keySet());
            for (Node node: keySetCopy){
                if (neighbourDegree.get(node) != clique.size()-1){
                    valueMapCOne.remove(node);
                }
            }
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
        for (Node node: graph.getNeighbours(removalNode)){
            if (neighbourDegree.keySet().contains(node)){
                if (neighbourDegree.get(node) > 1){
                    neighbourDegree.put(node, neighbourDegree.get(node) - 1);
                } else {
                    neighbourDegree.remove(node);
                }
            }
        }
        neighbourDegree.put(removalNode, clique.size());
    }

    private static void valueMapAdder(UndirectedGraph graph, Node addedNode, Map<Node, Integer> valueMapCZero, Map<Node, Integer> valueMapCOne){
        for (Node node: graph.getNeighbours(addedNode)){
            if (valueMapCZero.containsKey(node)){
                valueMapCZero.put(node, valueMapCZero.get(node)+1);
            }
            if (valueMapCOne.containsKey(node)){
                valueMapCOne.put(node, valueMapCOne.get(node)+1);
            }
        }
    }

    private static void valueMapRemover(UndirectedGraph graph, Node removedNode, Map<Node, Integer> valueMapCZero, Map<Node, Integer> valueMapCOne){
        for (Node node: graph.getNeighbours(removedNode)){
            if(valueMapCZero.containsKey(node)){
                valueMapCZero.put(node, valueMapCZero.get(node)-1);
            }
            if(valueMapCOne.containsKey(node)){
                valueMapCOne.put(node, valueMapCOne.get(node)-1);
            }
        }
    }

    private static int adjacencyToCOneCounter(UndirectedGraph graph, Node newCone, Set<Node> cZero){
        int value = 0;
        for (Node node: graph.getNeighbours(newCone)){
            if (cZero.contains(node)){
                value++;
            }
        }
        return value;
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

//    builds a UndirectedGraph class clique from a list of Nodes
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
