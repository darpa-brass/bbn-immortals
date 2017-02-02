package mil.darpa.immortals.analytics.protocols.log4j;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 1/20/17.
 */
public class AnalyticsEventReporter {

    public interface PythonInterface {
        @Headers("Content-Type: application/json")
        @POST("analytics/receiveEvents")
        Call<String> submitEvent(@Body String analyticsEvent);
    }

    private PythonInterface loggingInterface;

    private static AnalyticsEventReporter analyticsEventReporter = null;

    public static void initialize(String address, int port) {
        analyticsEventReporter = new AnalyticsEventReporter(address, port);
    }

    public static AnalyticsEventReporter getInstance() {
        return analyticsEventReporter;
    }

    public AnalyticsEventReporter(String address, int port) {
        String url = "http://" + address + ":" + Integer.toString(port) + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        loggingInterface = retrofit.create(PythonInterface.class);
    }

    public void report(String message) {
        try {
            Call<String> call = loggingInterface.submitEvent(message);
            call.execute();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
