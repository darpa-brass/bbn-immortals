package Helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by christia on 10/3/16.
 */
public class FileReaderUtil {
    public static List<String> ReadFileByLine(String filename) throws FileNotFoundException, IOException {
        BufferedReader br;
        List<String> lines = new ArrayList<String>() ;
        br = new BufferedReader(new FileReader(filename));
        String s;
        while ((s = br.readLine()) != null) {
            lines.add(s);
        }
        return lines;
    }
}
