package mil.darpa.immortals.testadapter.restendpoints;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetailsList;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedList;

/**
 * Created by awellman@bbn.com on 9/27/17.
 */
@Path("/dasListener")
public class DasRequestListener {

    private ImmortalsUtils.NetworkLogger logger = ImmortalsUtils.getNetworkLogger("TA", "DAS");
    private final TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();
    
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @POST
    @Path("/updateAdaptationStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAdaptationStatus(String ad) {
        // TODO: Validation
        // TODO: Figure out why objects extending Lists aren't deserializing properly
        AdaptationDetailsList adaptationDetails = gson.fromJson(ad, AdaptationDetailsList.class);
        logger.logPostReceived("/dasListener/updateAdaptationStatus", adaptationDetails);
        Response response = thm.updateAdaptationStatus(adaptationDetails);
        logger.logPostReceivedAckSending("/dasListener/updateAdaptationStatus", response.hasEntity() ? response.getEntity() : null);
        return response;
    }

    @POST
    @Path("/updateValidationStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateValidationStatus(String td) {
        // TODO: Validation
        // TODO: Figure out why objects extending Lists aren't deserializing properly
        TestDetailsList testDetails = gson.fromJson(td, TestDetailsList.class);
        logger.logPostReceived("/dasListener/updateValidationStatus", testDetails);
        Response response = thm.updateDeploymentTestStatus(testDetails);
        logger.logPostReceivedAckSending("/dasListener/updateValidationStatus", response.hasEntity() ? response.getEntity() : null);
        return response;
    }
}
