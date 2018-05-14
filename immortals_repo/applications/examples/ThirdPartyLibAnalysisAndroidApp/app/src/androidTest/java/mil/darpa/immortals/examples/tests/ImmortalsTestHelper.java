package mil.darpa.immortals.examples.tests;

import org.junit.Assert;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by awellman on 5/4/18.
 */

public class ImmortalsTestHelper {

    private static final LinkedList<String> assertionFailures = new LinkedList<>();

    private static final LinkedList<String> errors = new LinkedList<>();

    public static synchronized void disableNetwork() throws IOException, InterruptedException {
        toggleNetworkStatus(false);
    }

    public static synchronized void enableNetwork() throws IOException, InterruptedException {
        toggleNetworkStatus(true);
        Thread.sleep(2000);
    }

    private static synchronized void toggleNetworkStatus(boolean isEnabled) throws IOException, InterruptedException  {
        String newStatus = isEnabled ? "enable" : "disable";

        ProcessBuilder pb;
        Process p;
        int rval = -1;

        pb = new ProcessBuilder("su", "-c", "svc", "data", newStatus);
        pb.directory(new File("/storage/sdcard"));
        p = pb.start();
        rval = p.waitFor();
        if (rval != 0) {
            throw new RuntimeException("Failed setting data to " + newStatus + "!");
        }

        pb = new ProcessBuilder("su", "-c", "svc", "wifi", newStatus);
        pb.directory(new File("/storage/sdcard"));
        p = pb.start();
        rval = p.waitFor();
        if (rval != 0) {
            throw new RuntimeException("Failed setting wifi to " + newStatus + "!");
        }
    }

    public static synchronized void addError(String errorMessage) {
        errors.add(errorMessage);
    }

    public static synchronized boolean hasErrors() {
        return errors.size() > 0;
    }

    public static synchronized void addFailure(String failureMessage) {
        assertionFailures.add(failureMessage);
    }

    public static synchronized void finish() {
        for (String error : errors) {
            throw new RuntimeException(error);
        }
        for (String failure : assertionFailures) {
            Assert.fail(failure);
        }
        errors.clear();
        assertionFailures.clear();
    }
}
