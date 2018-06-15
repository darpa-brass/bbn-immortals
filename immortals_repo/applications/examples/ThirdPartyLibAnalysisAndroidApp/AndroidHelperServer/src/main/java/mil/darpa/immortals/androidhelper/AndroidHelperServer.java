package mil.darpa.immortals.androidhelper;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.IOException;

import static spark.Spark.post;

/**
 * Created by awellman@bbn.com on 5/31/18.
 */
public class AndroidHelperServer {
    private static synchronized void toggleNetworkStatus(boolean isEnabled) throws IOException, InterruptedException {
        String newStatus = isEnabled ? "enable" : "disable";

        ProcessBuilder pb;
        Process p;
        int rval = -1;

        pb = new ProcessBuilder("adb", "shell", "svc", "data", newStatus);
        p = pb.start();
        rval = p.waitFor();
        if (rval != 0) {
            throw new RuntimeException("Failed setting data to " + newStatus + "!");
        }

        pb = new ProcessBuilder("adb", "shell", "svc", "wifi", newStatus);
        p = pb.start();
        rval = p.waitFor();
        if (rval != 0) {
            throw new RuntimeException("Failed setting wifi to " + newStatus + "!");
        }
    }

    public static void main(String[] args) {
        Spark.ipAddress("0.0.0.0");

        post("/enableNetwork", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                try {
                    toggleNetworkStatus(true);
                    return "success";
                    
                } catch (Exception e) {
                    response.status(500);
                    return e.getMessage();
                }
            }
        });

        post("/disableNetwork", new Route() {
            @Override
            public Object handle(Request request, Response response) throws Exception {
                try {
                    toggleNetworkStatus(false);
                    return "success";
                    
                } catch (Exception e) {
                    response.status(500);
                    return e.getMessage();
                }
            }
        });
    }
}
