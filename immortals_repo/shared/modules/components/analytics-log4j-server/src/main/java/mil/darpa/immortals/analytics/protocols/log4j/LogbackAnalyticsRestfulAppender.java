package mil.darpa.immortals.analytics.protocols.log4j;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import java.io.IOException;

/**
 * Created by awellman@bbn.com on 7/31/17.
 */
public class LogbackAnalyticsRestfulAppender extends AppenderBase<ILoggingEvent> {

    public interface PythonInterface {
        @Headers("Content-Type: application/json")
        @POST("analytics/receiveEvents")
        Call<String> submitEvent(@Body String analyticsEvent);
    }

    private PythonInterface loggingInterface;

    LogbackAnalyticsRestfulAppender(String address, int port) {

        String url = "http://" + address + ":" + Integer.toString(port) + "/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        loggingInterface = retrofit.create(PythonInterface.class);
    }


    @Override
    protected void append(ILoggingEvent eventObject) {
        try {
            Call<String> call = loggingInterface.submitEvent(eventObject.getMessage());
            call.execute();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
