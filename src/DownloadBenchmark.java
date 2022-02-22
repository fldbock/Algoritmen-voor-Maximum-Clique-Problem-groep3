import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class DownloadBenchmark {
    public void main(){
        try (BufferedInputStream inputStream = new BufferedInputStream(new URL("https://iridia.ulb.ac.be/~fmascia/files/DIMACS/C500.9.clq").openStream());
             FileOutputStream fileOS = new FileOutputStream("file.txt")) {
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
