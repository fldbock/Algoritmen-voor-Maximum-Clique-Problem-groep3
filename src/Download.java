import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Download {
    public static void main(String[] args){
        String baseURl = "https://iridia.ulb.ac.be/~fmascia/files/DIMACS/"; //"https://iridia.ulb.ac.be/~fmascia/files/BHOSLIB/";
        List<String> DIMACSFiles = new ArrayList<>(List.of("C125.9", "C250.9", "C500.9", "C1000.9", "C2000.9", "DSJC1000_5", "DSJC500_5", "C2000.5", "C4000.5", "MANN_a27", "MANN_a45", "MANN_a81", "brock200_2", "brock200_4", "brock400_2", "brock400_4", "brock800_2", "brock800_4", "gen200_p0.9_44", "gen200_p0.9_55", "gen400_p0.9_55", "gen400_p0.9_65", "gen400_p0.9_75", "hamming10-4", "hamming8-4", "keller4", "keller5", "keller6", "p_hat300-1", "p_hat300-2", "p_hat300-3", "p_hat700-1", "p_hat700-2", "p_hat700-3", "p_hat1500-1", "p_hat1500-2", "p_hat1500-3"));
        List<String> BHOSLIBFiles = new ArrayList<>(List.of("frb30-15-1", "frb30-15-2", "frb30-15-3", "frb30-15-4", "frb30-15-5", "frb35-17-1", "frb35-17-2", "frb35-17-3", "frb35-17-4", "frb35-17-5", "frb40-19-1", "frb40-19-2", "frb40-19-3", "frb40-19-4", "frb40-19-5", "frb45-21-1", "frb45-21-2", "frb45-21-3", "frb45-21-4", "frb45-21-5", "frb50-23-1", "frb50-23-2", "frb50-23-3", "frb50-23-4", "frb50-23-5", "frb53-24-1", "frb53-24-2", "frb53-24-3", "frb53-24-4", "frb53-24-5", "frb59-26-1", "frb59-26-2", "frb59-26-3", "frb59-26-4", "frb59-26-5", "frb100-40"));
        for(int i = 0; i < DIMACSFiles.size(); i += 1){
            System.out.println(baseURl + DIMACSFiles.get(i) + ".clq");
            try (BufferedInputStream inputStream = new BufferedInputStream(new URL(baseURl + DIMACSFiles.get(i) + ".clq").openStream());
                 FileOutputStream fileOS = new FileOutputStream("C:/Users/flord/OneDrive/Documenten/GitHub/Algoritmen-voor-Maximum-Clique-Problem-groep3/src/DIMACSBenchmarkSet/" + DIMACSFiles.get(i) + ".txt")) {
                byte data[] = new byte[1024];
                int byteContent;
                while ((byteContent = inputStream.read(data, 0, 1024)) != -1) {
                    fileOS.write(data, 0, byteContent);
                }
            } catch (IOException e) {
                // handles IO exceptions
            }
        }
    }
}
