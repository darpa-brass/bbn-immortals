package mil.darpa.immortals.testadapter.restendpoints;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.ataklitemodel.ATAKLiteSubmissionModel;
import mil.darpa.immortals.core.api.ll.phase2.martimodel.MartiSubmissionModel;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 9/27/17.
 */
@Path("")
public class TestHarnessRequestListener {

    private Logger logger = LoggerFactory.getLogger(TestHarnessRequestListener.class);

    private ImmortalsUtils.NetworkLogger networkLogger = ImmortalsUtils.getNetworkLogger("TA", "TH");
    private final TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();

    @POST
    @Path("/action/databaseSchemaPerturbation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp1(SubmissionModel submissionModel) {
        networkLogger.logPostReceived("/action/databaseSchemaPerturbation", submissionModel);

        if (submissionModel == null) {
            logger.info("Empty submission model submitted. Assuming baseline where adaptation is NOT_APPLICABLE.");
            submissionModel = new SubmissionModel(
                    "I" + System.currentTimeMillis(),
                    new MartiSubmissionModel(),
                    null,
                    null
            );
            logger.info("Assigning adaptationIdentifier '" + submissionModel.sessionIdentifier + "' to baseline submission.");
        }
        
        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP1);
        networkLogger.logPostReceivedAckSending("/action/databaseSchemaPerturbation", response.hasEntity() ? response.getEntity() : null);
        return response;

    }

    @POST
    @Path("/action/crossApplicationDependencies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp2(SubmissionModel submissionModel) {
        networkLogger.logPostReceived("/action/crossApplicationDependencies", submissionModel);
        // TODO: Validation
        // TODO: Insert baseline submission model
        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP2);
        networkLogger.logPostReceivedAckSending("/action/crossApplicationDependencies", response.hasEntity() ? response.getEntity() : null);
        return response;
    }

    @POST
    @Path("/action/libraryEvolution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp3(SubmissionModel submissionModel) {
        networkLogger.logPostReceived("/action/libraryEvolution", submissionModel);
        // If null, assume baseline scenario with no adaptation
        // TODO: Add ATAKLite once its lack of tests (which is an error IMO for a primary artifact) doesn't cause the DAS to get upset
        if (submissionModel == null) {
            logger.info("Empty submission model submitted. Assuming baseline where adaptation is NOT_APPLICABLE.");
            submissionModel = new SubmissionModel(
                    "I" + System.currentTimeMillis(),
                    new MartiSubmissionModel(),
                    null,
                    null
            );
            logger.info("Assigning adaptationIdentifier '" + submissionModel.sessionIdentifier + "' to baseline submission.");
        }

        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP3);
        networkLogger.logPostReceivedAckSending("/query", response.hasEntity() ? response.getEntity() : null);
        return response;
    }


    @GET
    @Path("/alive")
    public Response alive() {
        networkLogger.logGetReceived("/alive", null);
        Response response = thm.thGetAlive();
        networkLogger.logGetReceivedAckSending("/alive", response.hasEntity() ? response.getEntity() : null);
        return response;
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response query() {
        networkLogger.logGetReceived("/query", null);
        Response response = thm.thGetQuery();
        networkLogger.logGetReceivedAckSending("/query", response.hasEntity() ? response.getEntity() : null);
        return response;
    }

    @POST
    @Path("/enabled")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enabled(EnableDas enableDas) {
        // TODO: Validation
        networkLogger.logPostReceived("/enabled", enableDas);
        Response response = thm.thPostEnabled(enableDas);
        networkLogger.logPostReceivedAckSending("/enabled", response.hasEntity() ? response.getEntity() : null);
        return response;
    }
}
