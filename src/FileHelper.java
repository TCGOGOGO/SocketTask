import java.io.*;

/**
 * Created by tcgogogo on 17/1/6.
 */
public class FileHelper {

    public File file;
    public static String dir = "/Users/tcgogogo/Desktop/Java/MySocket/data/";

    public FileHelper(String fileName) throws IOException {
        file = new File(dir + fileName);
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    public void writeToFile(String content) throws FileNotFoundException {
        FileOutputStream fileOut = new FileOutputStream(file, true);
        try {
            fileOut.write(content.getBytes("utf-8"));
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void readFromFile() throws IOException {
        FileInputStream fileIn = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fileIn));
        String line = "";
        while((line = br.readLine()) != null) {
            System.out.println(line);
        }
        br.close();
    }
}
