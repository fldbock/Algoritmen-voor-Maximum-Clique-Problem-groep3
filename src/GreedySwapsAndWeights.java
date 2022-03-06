import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;

import java.util.*;


public class GreedySwapsAndWeights {
    public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of("C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5", "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for (int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            List<Node> clique = procedureDAGS(graph);
            System.out.println(testFiles.get(i) + ": " + clique.size() + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }


    public static List<Node> procedureDAGS(UndirectedGraph graph){
        int MAX_ITER = Math.round(graph.getAllNodes().size() /8); //maximum number of iterations of greedy_weighted that is run before moving on.
        int MAX_WEIGHT = 3; //maximum number of times weight is reduced before setting to 0.
        int START_SWAP = 5; //nodes to add before consideren swapping clique nodes.
        int MAX_CURRENT_SWAPS = 5; //maximum amount of swaps before a node must be added without swapping.
        double DELTA = 0.1; //least used nodes fraction of total nodes used as startnodes in greedy_weighted
        List<Node> clique;
        List<Node> bestClique = new ArrayList<>();
        Map<Node, Integer> countInCliques = new HashMap<>(); //counter for frequency a node is used in a final clique from greedy_swap.
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
            Node startNode = Collections.min(countInCliques.entrySet(), Map.Entry.comparingByValue()).getKey(); //select the least used node from greedy_swap as startNode
            countInCliques.remove(startNode); //remove startnode as to not consider it again
            for (Node graphNode: graph.getAllNodes()){ //build node weights
                nodeWeights.put(graphNode, 1.0);
            }
            for (int t = 1; t < MAX_ITER; t++) { //run greedy_weighted MAX_ITER times, updating weights each run
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
        //initialisation: a copy of the original node weights map is made, which is trimmed to form the candidate nodes set.
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
            //update nodeWeights to only keep nodes attached to the popular node.
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
        Map<Node, Integer> neighbourDegree = new HashMap<>(); //Map that tracks every the adjacency level of each nodes to the current clique.
        Map<Node, Integer> valueMapCZero = new HashMap<>(); //Map that tracks the degree of connection to C_0 nodes for all nodes in C_0. this is used as selection criteria.
        Map<Node, Integer> valueMapCOne = new HashMap<>(); //Same as above for C_1.
        for (Node node:graph.getNeighbours(startnode)){ //construct initial neighbourDegree and valueMap C_0
            neighbourDegree.put(node, 1);
            valueMapAdder(graph, node, valueMapCZero, valueMapCOne);
            valueMapCZero.put(node, adjacencyToCOneCounter(graph, node, valueMapCZero.keySet()));
        }
        int currentSwaps = 0;
        int totalSwaps = 0;
        Node lastSwapNode = null;
        Node removalNode = null;

        //clique building loop
        while(clique.size() == Collections.max(neighbourDegree.values())){ //if there is a neighbour of the clique with degree of adjacency to the clique equal to clique size, it can be added to the clique
            //Find the best node to add or swap based on adjacency to the C_0.
            int bestNodeValue = -1; //we cannot simply use .get(popularNode) because it is unknown which map popularNode is from. Can be replaced with a Boolean.
            boolean swap = false;
            Node popularNode = null;
            for(Node node : valueMapCZero.keySet()){
                if (valueMapCZero.get(node)>bestNodeValue){
                    popularNode = node;
                    bestNodeValue = valueMapCZero.get(node);
                }
            }
            if(totalSwaps >= START_SWAP & currentSwaps < MAX_CURRENT_SWAPS) { //criterion to permit swapping.
                for (Node node : valueMapCZero.keySet()){
                    if (valueMapCOne.get(node)>bestNodeValue & node != lastSwapNode){
                        popularNode = node;
                        bestNodeValue = valueMapCOne.get(node);
                        swap = true;
                    }
                }
            }
            //remove a node is swap is TRUE. Swapping is done by a removal procedure followed by the same adding procedure as a non-swap addition.
            if(swap) {
                for (Node node : clique) { //finding the node to be removed from the clique.
                    if (!graph.getNeighbours(popularNode).contains(node)) {
                        removalNode = node;
                        break;
                    }
                }
                List <Node> cOneNewList = new ArrayList<>(); //used to track C_2 nodes that will become C_1 nodes.
                for (Node node: neighbourDegree.keySet()){
                    if (neighbourDegree.get(node)==clique.size()-2){
                        cOneNewList.add(node);
                    }
                }
                clique.remove(removalNode);
                valueMapCZero.put(removalNode, valueMapCZero.size()); //add the removed node to the C_0 value map.
                for (Node node : valueMapCZero.keySet()){ //add 1 to every value of OLD pre-removal C_0 nodes.
                    valueMapCZero.put(node, valueMapCZero.get(node)+1);
                }
                neighbourDegreeRemover(graph, removalNode, clique, neighbourDegree); //lower the adjacency degree to the clique of its neighbours.
                for (Node node: neighbourDegree.keySet()){ //remove C_2 nodes that stayed C_2 nodes
                    if (neighbourDegree.get(node)==clique.size()-2 & cOneNewList.contains(node)){
                        cOneNewList.remove(node);
                    }
                }
                for (Node node : valueMapCOne.keySet()){ //if a node belonged to C_1 prior to removal of the removalnode, then either it jumps to C_0, or its value increases by 1.
                    if (neighbourDegree.get(node) == clique.size()){
                        valueMapAdder(graph, node, valueMapCZero, valueMapCOne); //if a node jumps to C_0, the values of C_0 and C_1 nodes need adjusting.
                        valueMapCZero.put(node, valueMapCOne.get(node));
                        valueMapCOne.remove(node);
                    } else {
                        valueMapCOne.put(node, valueMapCOne.get(node)+1);
                    }
                }
                for (Node node: cOneNewList){ //add nodes that are newly C_1 (jumped up from C_2)
                    valueMapCOne.put(node, adjacencyToCOneCounter(graph, node, valueMapCZero.keySet()));
                }
                lastSwapNode = removalNode;
                currentSwaps++;
                totalSwaps++;
            } else {
                currentSwaps = 0;
            }

            //add a node. By adding a node, nodes can only drop in C_n sets, not climb. i.e. a C_1 node can only become C_2 or stay C_1, not become C_0.
            clique.add(popularNode);
            neighbourDegree.remove(popularNode); //popularNode is no longer a neighbour of the clique.
            neighbourDegreeAdder(graph, popularNode, clique, neighbourDegree); //update clique-neighbour-degrees for each neighbour of popularNode.
            valueMapCZero.remove(popularNode); //popularNode is no longer in C_0.
            valueMapRemover(graph, popularNode, valueMapCZero, valueMapCOne); //A node is removed from C_0, so values need updating
            Set<Node> keySetCopy = new HashSet<>(valueMapCZero.keySet()); //avoid concurrent modification exception
            for (Node node:keySetCopy){
                if (neighbourDegree.get(node) != clique.size()){ //if the C_0 node should no longer belong to C_0 but now belongs to C_1, it is removed and added to C_1, and other values are updated
                    valueMapRemover(graph, node, valueMapCZero, valueMapCOne);
                    valueMapCOne.put(node, valueMapCZero.get(node));
                    valueMapCZero.remove(node);
                }
            }
            keySetCopy = new HashSet<>(valueMapCOne.keySet()); //avoid concurrent modification exception
            for (Node node: keySetCopy){
                if (neighbourDegree.get(node) != clique.size()-1){ //if a C_1 node no longer belongs to C_1, it has dropped to C_2 and should be removed from C_1. no values need updating.
                    valueMapCOne.remove(node);
                }
            }
        }

        return clique;
    }

    //Updates the amount of connections to the clique for every neighbour of the added node.
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

    //updates the value maps of C_0 and C_1 when a node addedNode was added to C_0. Does not add or remove nodes from the maps, this is done separately.
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

    //updates the value maps of C_0 and C_1 when a node removedNode is removed from C_0. Does not add or remove nodes from the maps, this is done seperately.
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

    //counts the amount of nodes in C_0 that a node is adjacent to. This does not need to be a C_1 node despite the name.
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

}
