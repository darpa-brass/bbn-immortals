package mil.darpa.immortals.testadapter.restendpoints;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by awellman@bbn.com on 9/28/17.
 */
public interface DasSubmissionInterface {
    @POST("bbn/das/submitAdaptationRequest")
    @Headers("Content-Type: text/plain")
    public Call<String> submitAdaptationRequest(@Body String rdf);

    @POST("bbn/das/submitValidationRequest")
    @Headers("Content-Type: text/plain")
    public Call<String> submitValidationRequest(@Body String rdf);

}
