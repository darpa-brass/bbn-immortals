package mil.darpa.immortals.core.das.restendpoints;

import mil.darpa.immortals.ImmortalsUtils;
import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.das.*;
import mil.darpa.immortals.core.das.sparql.SessionIdentifier;
import mil.darpa.immortals.core.das.upgrademodules.LibraryUpgradeModule;
import mil.darpa.immortals.das.context.ContextManager;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.file.Files;

@Path("das")
public class DASEndpoint {

    static final Logger logger = LoggerFactory.getLogger(DASEndpoint.class);

    static final ImmortalsUtils.NetworkLogger networkLogger = ImmortalsUtils.getNetworkLogger("DAS", "TA");

    @GET
    @Path("/test")
    @Produces(MediaType.TEXT_PLAIN)
    public String test() {

        logger.trace("/test REST endpoint invoked.");

        return "Basic REST services working with DAS.";
    }

    @GET
    @Path("/sut-about")
    @Produces(MediaType.APPLICATION_JSON)
    public SUTInformation getAbout() {

        logger.trace("/sut-about REST endpoint invoked.");

        return DAS.getSUTInformation();
    }

    @GET
    @Path("/das-status")
    @Produces(MediaType.APPLICATION_JSON)
    public DASStatusValue getDASStatus() {

        logger.trace("/das-status REST endpoint invoked.");

        return DAS.getStatus();
    }

    @POST
    @Path("/submitAdaptationRequest")
    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.APPLICATION_JSON)
    public Response submitAdaptationRequest(String rdf) {
        // Helper class for network logging. Controls information display level based on logging level and
        // standardizes send/receive/ack message formats and logging endpoint
        networkLogger.logPostReceived("/bbn/das/submitAdaptationRequest", rdf);

        try {
            // Create the file
            java.nio.file.Path rdfPath =
                    ImmortalsConfig.getInstance().extensions.getProducedTtlOutputDirectory().resolve("deploymentModel.ttl");
            Files.write(rdfPath, rdf.getBytes());

            AdaptationManager am = AdaptationManager.getInstance();
            String knowledgeRepoGraphUri = am.initializeKnowledgeRepo();

            String adaptationIdentifier = SessionIdentifier.select(knowledgeRepoGraphUri);
            
            // Also save the RDF in logs for debugging
            rdfPath = 
                    ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(adaptationIdentifier).resolve("deploymentModel.ttl");
            Files.write(rdfPath, rdf.getBytes());

            DasAdaptationContext dac = ContextManager.getContext(adaptationIdentifier,
                    knowledgeRepoGraphUri, knowledgeRepoGraphUri);


            Runnable adaptationTask = () -> {
                am.triggerAdaptation(dac);
            };
            Thread t = new Thread(adaptationTask);

            // A fatal exception handler also exists in the error handler to bubble problems up as necessary
            t.setUncaughtExceptionHandler(ImmortalsErrorHandler.fatalExceptionHandler);
            t.start();

            // Helper class for network logging. Controls information validity based on logging level and
            // standardizes send/receive/ack message formats and logging endpoint
            networkLogger.logPostReceivedAckSending("/bbn/das/submitAdaptationRequest", adaptationIdentifier);
            return Response.ok().build();
        } catch (Exception e) {
            ImmortalsErrorHandler.reportFatalException(e);
            return Response.serverError().entity(e.toString()).build();
        }
    }

    @POST
    @Path("/submitValidationRequest")
    @Consumes(MediaType.TEXT_PLAIN)
//    @Produces(MediaType.APPLICATION_JSON)
    public Response submitValidationRequest(String rdf) {
        // Helper class for network logging. Controls information display level based on logging level and
        // standardizes send/receive/ack message formats and logging endpoint
        networkLogger.logPostReceived("/bbn/das/submitValidationRequest", rdf);

        try {
            // Create the file
            java.nio.file.Path rdfPath =
                    ImmortalsConfig.getInstance().extensions.getProducedTtlOutputDirectory().resolve("deploymentModel.ttl");
            Files.write(rdfPath, rdf.getBytes());

            AdaptationManager am = AdaptationManager.getInstance();
            String knowledgeRepoGraphUri = am.initializeKnowledgeRepo();

            String adaptationIdentifier = SessionIdentifier.select(knowledgeRepoGraphUri);
            DasAdaptationContext dac = ContextManager.getContext(adaptationIdentifier,
                    knowledgeRepoGraphUri, knowledgeRepoGraphUri);

            // Also save the RDF in logs for debugging
            rdfPath =
                    ImmortalsConfig.getInstance().globals.getAdaptationLogDirectory(adaptationIdentifier).resolve("deploymentModel.ttl");
            Files.write(rdfPath, rdf.getBytes());
            
            // TODO: This should probably match with the AdaptationManager...
            LibraryUpgradeModule lum = new LibraryUpgradeModule();
            if (lum.isApplicable(dac)) {
                lum.apply(dac);
            }

            ValidationManager vm = new ValidationManager(dac);
            
            Runnable validationTask = () -> {
                try {
                    TestCaseReportSet tcrs = vm.executeValidation(true);
                } catch (Exception e) {
                    ImmortalsErrorHandler.reportFatalException(e);
                }
            };

            Thread t = new Thread(validationTask);

            // A fatal exception handler also exists in the error handler to bubble problems up as necessary
            t.setUncaughtExceptionHandler(ImmortalsErrorHandler.fatalExceptionHandler);
            t.start();

            // Helper class for network logging. Controls information validity based on logging level and
            // standardizes send/receive/ack message formats and logging endpoint
            networkLogger.logPostReceivedAckSending("/bbn/das/submitValidationRequest", adaptationIdentifier);
            return Response.ok().build();
        } catch (Exception e) {
            ImmortalsErrorHandler.reportFatalException(e);
            return Response.serverError().entity(e.toString()).build();
        }
    }

}
