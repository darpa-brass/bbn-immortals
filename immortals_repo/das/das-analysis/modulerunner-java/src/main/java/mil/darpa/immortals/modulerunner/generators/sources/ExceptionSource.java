package mil.darpa.immortals.modulerunner.generators.sources;

import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by awellman@bbn.com on 6/6/16.
 */
public class ExceptionSource {

    public ExceptionSource() {

    }

    public byte[] getBytes() {
        int randNum = ThreadLocalRandom.current().nextInt(4);

        try {
            if (randNum == 0) {
                throw new NullPointerException("Throwing null pointer exception intentionally!");
            } else if (randNum == 1) {
                throw new IOException("Throwing IO Exception intentionally!");
            } else if (randNum == 2) {

                throw new ArrayIndexOutOfBoundsException("Throwing intentional exception!");
            } else {
                throw new ClassCastException("Intentional exception being thrown!");
            }
        } catch (Exception e) {
            StackTraceElement[] st = e.getStackTrace();
            String logMessage = "";

            for (StackTraceElement ste : st) {
                logMessage += ste.toString();
            }

            return logMessage.getBytes();
        }
    }
}
