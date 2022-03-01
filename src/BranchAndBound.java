import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;


public class BranchAndBound {
    private static int beste = 0;

    public static void bAndB(UndirectedGraph G, List<Node> toppen, int diepte){
        int m = 0;

        if(!toppen.isEmpty()){
            while(m < toppen.size()-1){
                if(diepte + toppen.size()-1-m > beste){
                    List<Node> toppenNieuw = new ArrayList<>();
                    for(int j = m+1; j < toppen.size(); j++){
                        if(G.getNeighbours(toppen.get(m)).contains(toppen.get(j))){
                            toppenNieuw.add(toppen.get(j));
                        }
                    }
                    bAndB(G, toppenNieuw, diepte+1);
                } else {
                    m = toppen.size()-2;
                }
                m++;
            }
            if(diepte> beste) {
                beste = diepte;
            }
        } else {
            diepte--;
            if( diepte > beste) {
                beste = diepte;
            }
        }

    }

    public static int maximumKliek(UndirectedGraph G){
        Comparator<Node> nodeComparator = (node1, node2) -> {
            int n1 = G.getDegree(node1); int n2 = G.getDegree(node2);
            return Integer.compare(n1, n2);
        };
        List<Node> toppen = G.getAllNodes();
        toppen.sort(nodeComparator);
        beste = 0;
        bAndB(G, toppen, 1);
        return beste;

    }

    public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for(int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int best = maximumKliek(graph);
            System.out.println(testFiles.get(i) + ": " + best  + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }
}

