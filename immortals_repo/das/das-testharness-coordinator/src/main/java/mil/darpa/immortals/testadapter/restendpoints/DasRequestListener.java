package mil.darpa.immortals.testadapter.restendpoints;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
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

    @POST
    @Path("/updateAdaptationStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateAdaptationStatus(AdaptationDetails adaptationDetails) {
        // TODO: Validation
        logger.logPostReceived("/dasListener/updateAdaptationStatus", adaptationDetails);
        Response response = thm.updateAdaptationStatus(adaptationDetails);
        logger.logPostReceivedAckSending("/dasListener/updateAdaptationStatus", response.hasEntity() ? response.getEntity() : null);
        return response;
    }

    @POST
    @Path("/updateValidationStatus")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateValidationStatus(TestDetails testDetails) {
        // TODO: Validation
        logger.logPostReceived("/dasListener/updateValidationStatus", testDetails);
        LinkedList<TestDetails> td = new LinkedList<>();
        td.add(testDetails);
        Response response = thm.updateDeploymentTestStatus(td);
        logger.logPostReceivedAckSending("/dasListener/updateValidationStatus", response.hasEntity() ? response.getEntity() : null);
        return response;
    }
}
