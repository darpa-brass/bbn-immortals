package mil.darpa.immortals.testadapter.restendpoints;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 9/27/17.
 */
@Path("")
public class TestHarnessRequestListener {

    private ImmortalsUtils.NetworkLogger logger = new ImmortalsUtils.NetworkLogger("TA", "DAS");
    private final TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();

    @POST
    @Path("/action/databaseSchemaPerturbation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp1(SubmissionModel submissionModel) {
        logger.logPostReceived("/action/databaseSchemaPerturbation", submissionModel);
        // TODO: Validation
        // TODO: Insert baseline submission model
        if (submissionModel == null) {
            submissionModel = new SubmissionModel();
        }
        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP1);
        logger.logPostReceivedAckSending("/action/databaseSchemaPerturbation", response);
        return response;

    }

    @POST
    @Path("/action/crossApplicationDependencies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp2(SubmissionModel submissionModel) {
        logger.logPostReceived("/action/crossApplicationDependencies", submissionModel);
        // TODO: Validation
        // TODO: Insert baseline submission model
        if (submissionModel == null) {
            submissionModel = new SubmissionModel();
        }
        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP2);
        logger.logPostReceivedAckSending("/action/crossApplicationDependencies", response);
        return response;
    }

    @POST
    @Path("/action/libraryEvolution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response cp3(SubmissionModel submissionModel) {
        logger.logPostReceived("/action/libraryEvolution", submissionModel);
        // TODO: Validation
        // TODO: Insert baseline submission model
        if (submissionModel == null) {
            submissionModel = new SubmissionModel();
        }
        Response response = thm.submitSubmissionModel(submissionModel, TestHarnessAdapterMediator.ChallengeProblem.P2CP3);
        logger.logPostReceivedAckSending("/query", response.getEntity());
        return response;
    }
    

    @GET
    @Path("/alive")
    public Response alive() {
        logger.logGetReceived("/alive", null);
        Response response = thm.thGetAlive();
        logger.logGetReceivedAckSending("/alive", response.getEntity());
        return response;
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response query() {
        logger.logGetReceived("/query", null);
        Response response = thm.thGetQuery();
        logger.logGetReceivedAckSending("/query", response.getEntity());
        return response;
    }

    @POST
    @Path("/enabled")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enabled(EnableDas enableDas) {
        // TODO: Validation
        logger.logPostReceived("/enabled", enableDas);
        Response response = thm.thPostEnabled(enableDas);
        logger.logPostReceivedAckSending("/enabled", response.getEntity());
        return response;
    }
}
