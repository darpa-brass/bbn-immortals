package mil.darpa.immortals.testadapter.restendpoints;

import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.das.context.DasAdaptationContext;
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
    public Call<AdaptationDetails> submitAdaptationRequest(@Body String rdf);
    
    
}
