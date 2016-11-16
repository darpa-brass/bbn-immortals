package Helper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by arpit on 8/15/16.
 */
public class FileOperationUtil {
    public static boolean isEmptyFile(String filename) throws FileNotFoundException, IOException {
        BufferedReader br;
        br = new BufferedReader(new FileReader(filename));
        if (br.readLine() == null) {
            return true;
        }
        return false;
    }
}
