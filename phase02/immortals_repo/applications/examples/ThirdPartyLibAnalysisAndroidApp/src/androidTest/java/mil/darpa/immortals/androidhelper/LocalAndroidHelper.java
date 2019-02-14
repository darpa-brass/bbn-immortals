package mil.darpa.immortals.androidhelper;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.POST;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman on 5/4/18.
 */

public class LocalAndroidHelper {
    
    private interface AndroidHelperService {
        @POST("/enableNetwork")
        Call<Void> enableNetwork();

        @POST("/disableNetwork")
        Call<Void> disableNetwork();
    }

    private static AndroidHelperService _androidHelperService;

    private synchronized static AndroidHelperService getAndroidHelperService() throws IOException {
        if (_androidHelperService == null) {
            Gson gson = new Gson();

            FileReader fr = new FileReader(new File("/sdcard/deployment_config.json"));
            JsonObject configData = gson.fromJson(fr, JsonObject.class);

            Retrofit retrofit = new Retrofit.Builder().baseUrl("http://" + configData.get("martiAddress").getAsString() + ":4567").build();
            _androidHelperService = retrofit.create(AndroidHelperService.class);
        }
        return _androidHelperService;
    }

    private static final LinkedList<String> assertionFailures = new LinkedList<>();

    private static final LinkedList<String> errors = new LinkedList<>();

    public static synchronized void disableNetwork() throws IOException, InterruptedException {
        try {
            // TODO: Add some sort of check so that this doesn't possibly create a false failure
            Response r = getAndroidHelperService().disableNetwork().execute();
        } catch (SocketException e) {
            // Do nothing. It worked. You can't get a response after disabling the network.
        }
    }

    public static synchronized void enableNetwork() throws IOException, InterruptedException {
        Response r = getAndroidHelperService().enableNetwork().execute();
        if (!r.isSuccessful()) {
            toggleNetworkStatus(true);
        }
        Thread.sleep(4000);
    }

    private static synchronized void toggleNetworkStatus(boolean isEnabled) throws IOException, InterruptedException  {
        String newStatus = isEnabled ? "enable" : "disable";

        ProcessBuilder pb;
        Process p;
        int rval = -1;

        pb = new ProcessBuilder("su", "-c", "svc", "data", newStatus);
        pb.directory(new File("/sdcard"));
        p = pb.start();
        rval = p.waitFor();
        if (rval != 0) {
            throw new RuntimeException("Failed setting data to " + newStatus + "!");
        }

        pb = new ProcessBuilder("su", "-c", "svc", "wifi", newStatus);
        pb.directory(new File("/sdcard"));
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

    public static synchronized List<String> getAndClearFailures() {
        List<String> rval = new LinkedList<>();
        
        for (String error : errors) {
            throw new RuntimeException(error);
        }
        
        rval.addAll(assertionFailures);
        
        errors.clear();
        assertionFailures.clear();
        
        return rval;
    }
}
