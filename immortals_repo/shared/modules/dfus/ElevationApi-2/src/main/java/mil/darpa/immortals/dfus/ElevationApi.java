package mil.darpa.immortals.dfus;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 12/6/17.
 */
public class ElevationApi {

    // Using dummy URL for initial scenario for ease of failure
//    private final String baseUrl = "https://maps.googleapis.com";
    private final String baseUrl = "https://maps.googleapis.notarealapi.bbn.com";

    // Mutation testing would kill a demo key in a matter of minutes probably, so not including a key here for now...
    private final String apiKey = "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    private GoogleApiService gas;

    private class Result {

        private class ElevationResult {

            public double elevation;

            public ElevationResult() {

            }
        }

        public LinkedList<ElevationResult> results;
        public String status;

        public Result() {

        }
    }

    interface GoogleApiService {
        @GET("maps/api/elevation/json")
        public Call<Result> getElevation(@Query("locations") String latCommaLon, @Query("key") String key);
    }

    public void init() {
        Retrofit retrotfit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        gas = retrotfit.create(GoogleApiService.class);
    }

    public ElevationData getElevation(double x, double y) {
        try {
            Call<Result> c = gas.getElevation((y + "," + x), apiKey);
            Response<Result> r = c.execute();
            double value = r.body().results.get(0).elevation;
            return new ElevationData(x, y, value, 1000);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
