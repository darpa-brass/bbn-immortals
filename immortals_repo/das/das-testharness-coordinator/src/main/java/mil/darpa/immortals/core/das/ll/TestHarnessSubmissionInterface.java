package mil.darpa.immortals.core.das.ll;

import mil.darpa.immortals.core.api.ll.phase2.result.TestAdapterState;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public interface TestHarnessSubmissionInterface {

    @POST("/ready")
    public Call<Void> ready();

    @POST("/error")
    @Headers("Content-Type: text/plain")
    public Call<Void> error(@Body String value);

    @POST("/status")
    @Headers("Content-Type: application/json")
    public Call<Void> status(@Body TestAdapterState testAdapterState);

    @POST("/done")
    @Headers("Content-Type: application/json")
    public Call<Void> done(@Body TestAdapterState testAdapterState);

}
