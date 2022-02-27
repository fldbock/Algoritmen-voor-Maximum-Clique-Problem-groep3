import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

// Het algoritme valt het best te snappen door het voorbeeld op pagina 2 van de Branch and Bound paper te volgen.

public class BranchAndBound {
    // Het algoritme werkt adhv de parameter diepte. Diepte 1 is wanneer we 1 top bekijken, diepte 2 is wanneer we de adjacente toppen bekijken,
    // diepte 3 is wanneer we de adjacente toppen van een adjacente top bekijken, die bovendien ook nog steeds adjacent zijn met de eerste top enzoverder.
    // Zo wordt een kliek opgebouwd.
    private static int beste = 0;

    public static void bAndB(UndirectedGraph G, List<Node> toppen, int diepte){
        int m = 0;

        if(!toppen.isEmpty()){
            while(m < toppen.size()-1){
                if(diepte + toppen.size()-1-m > beste){
                    // Door een nieuwe lijst van adjacente toppen uit de huidige lijst te maken, gaan we een niveau dieper. Met deze lijst voeren we
                    // het algoritme opnieuw uit.
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
            // Als we op het einde van een lijst zitten, kunnen we niet meer verder en hebben we een maximale kliek gevonden.
            if(diepte> beste) {
                beste = diepte;
            }
        } else if( diepte -1 > beste) {
            beste = diepte -1;
        }

    }

    public static int maximumKliek(UndirectedGraph G){
        bAndB(G, G.getAllNodes(), 1);
        return beste;

    }

    public static void main(String[] args){
        ReadGraph rg = new ReadGraph();
        List<String> testFiles = new ArrayList<>(List.of(/*"C125.9", "C250.9","DSJC1000_5", "DSJC500_5", "C2000.5",*/ "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "hamming10-4", "hamming8-4", "keller4", "keller5"));
        for(int i = 0; i < testFiles.size(); i++){
            UndirectedGraph graph = rg.readGraph("DIMACSBenchmarkSet", testFiles.get(i));
            long startTime = System.currentTimeMillis();
            int beste = maximumKliek(graph);
            System.out.println(testFiles.get(i) + ": " + beste  + ": " + String.valueOf(System.currentTimeMillis()-startTime));
        }
    }
}

