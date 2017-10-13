package mil.darpa.immortals.core.api;

import com.google.gson.Gson;

/**
 * Created by awellman@bbn.com on 8/31/17.
 */
public class ApiHelper {
    
    private static Gson gson;
    
    public static Gson getSharedGson() {
        if (gson == null) {
            gson = new Gson();
        }
        return gson;
    }
}
