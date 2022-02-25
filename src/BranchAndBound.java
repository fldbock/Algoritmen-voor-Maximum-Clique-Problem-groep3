import graphlib.graphs.UndirectedGraph;
import graphlib.nodes.Node;
import java.util.*;

// Het algoritme valt het best te snappen door het voorbeeld op pagina 2 van de Branch and Bound paper te volgen.

public class BranchAndBound {
    // BestClique zal uiteindelijk de lijst weergeven van een maximum kliek in de graaf.
    private List<Node> BestClique = new ArrayList<>();

    // In de functie geven we de graaf, een lijst toppen, de huidige diepte en de huidige beste kliek (current best clique CBC) mee.
    // Voor een nieuwe graaf beginnen we met de volledige lijst van alle toppen, diepte 1 en CBC een lege lijst.

    public void BAndB(UndirectedGraph G, List<Node> toppen, int diepte, List<Node> CBC){
        int m = 0;
        // Het algoritme werkt adhv de parameter diepte. Diepte 1 is wanneer we 1 top bekijken, diepte 2 is wanneer we de adjacente toppen bekijken,
        // diepte 3 is wanneer we de adjacente toppen van een adjacente top bekijken, die bovendien ook nog steeds adjacent zijn met de eerste top enzoverder.
        // Zo wordt een kliek opgebouwd.
        // De boundfunctie prunet de zoektocht indien de maximale mogelijke grootte van de huidige kliek de grootte van de beste niet kan overschrijden.
        if(!toppen.isEmpty()){
            while(m < toppen.size()-1 && diepte + toppen.size()-1-m > BestClique.size()){
                // Door een nieuwe lijst van adjacente toppen uit de huidige lijst te maken, gaan we een niveau dieper. Met deze lijst voeren we
                // het algoritme opnieuw uit.
                List<Node> toppenNieuw = new ArrayList<>();
                for(int j = m+1; j < toppen.size(); j++){
                    if(G.getNeighbours(toppen.get(m)).contains(toppen.get(j))){
                        toppenNieuw.add(toppen.get(j));
                    }
                }
                CBC.add(toppen.get(m));
                BAndB(G, toppenNieuw, diepte+1, CBC);
                CBC.remove(toppen.get(m));
                m++;
            }
            // Als we op het einde van een lijst zitten, kunnen we niet meer verder en hebben we een maximale kliek gevonden.
            if(m == toppen.size()-1){
                CBC.add(toppen.get(m));
                if(CBC.size()> BestClique.size()) {
                    BestClique = CBC;
                }
                CBC.remove(toppen.get(m));
            }
        } else if(CBC.size()> BestClique.size()) {
            BestClique = CBC;
        }

    }

    public List<Node> MaximumKliek(UndirectedGraph G){
        List<Node> CBC = new ArrayList<>();
        BAndB(G, G.getAllNodes(), 1, CBC);
        return BestClique;

    }


}

