package mil.darpa.immortals.core.das.restendpoints;

import mil.darpa.immortals.core.api.ll.phase2.EnableDas;
import mil.darpa.immortals.core.api.ll.phase2.SubmissionModel;
import mil.darpa.immortals.core.das.ll.TestHarnessAdapterMediator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by awellman@bbn.com on 9/27/17.
 */
@Path("/")
public class TestHarnessRequestListener {

    private final TestHarnessAdapterMediator thm = TestHarnessAdapterMediator.getInstance();

    @POST
    @Path("action/databaseSchemaPerturbation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cp1(SubmissionModel submissionModel) {
        // TODO: Validation
        return thm.thPostP2CP1(submissionModel);
    }

    @POST
    @Path("action/crossApplicationDependencies")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cp2(SubmissionModel submissionModel) {
        // TODO: Validation
        return thm.thPostP2CP2(submissionModel);
    }

    @POST
    @Path("action/libraryEvolution")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response cp3(SubmissionModel submissionModel) {
        // TODO: Validation
        return thm.thPostP2CP3(submissionModel);
    }

    @GET
    @Path("/alive")
    public Response alive() {
        return thm.thGetAlive();
    }

    @GET
    @Path("/query")
    @Produces(MediaType.APPLICATION_JSON)
    public synchronized Response query() {
        return thm.thGetQuery();
    }

    @POST
    @Path("/enabled")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response enabled(EnableDas enableDas) {
        // TODO: Validation
        return thm.thPostEnabled(enableDas);
    }
}
