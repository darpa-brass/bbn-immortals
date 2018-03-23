package mil.darpa.immortals.core.das;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationmodules.schemaevolution.SchemaEvolutionAdapter;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AdaptationManager {

    static final Logger logger = LoggerFactory.getLogger(AdaptationManager.class);
    private List<IAdaptationModule> adaptationModules = new ArrayList<>();

    static {
        instance = new AdaptationManager();
    }

    private AdaptationManager() {

        adaptationModules.add(new SchemaEvolutionAdapter());
    }

    public static AdaptationManager getInstance() {
        return instance;
    }

    public void triggerAdaptation(DasAdaptationContext dac) {

        IAdaptationModule currentModule = null;
        try {
            List<IAdaptationModule> toExecute = new LinkedList<>();

            // Submit initial test details
            ValidationManager vm = new ValidationManager(dac);
            TestDetails schemaEvolutionTestDetails = vm.queryAndReportValidatiors().get(0);

            // TODO: Manage queuing of messages in TA instead of this sleep!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // First, determine which adaptation modules will be run
            for (IAdaptationModule am : adaptationModules) {
                currentModule = am;
                if (am.isApplicable(dac)) {
                    toExecute.add(am);

                    // If it will be run, submit an initial status
                    AdaptationDetails ad = new AdaptationDetails(
                            am.getClass().getName(),
                            DasOutcome.PENDING, dac.getAdaptationIdentifer());
                    dac.submitAdaptationStatus(ad);
                    logger.info("Adaptation Module (" + am.getClass().getName() + ") is applicable.");
                } else {
                    logger.info("Adaptation Module (" + am.getClass().getName() + ") not applicable.");
                }
            }

            currentModule = null;

            // TODO: Manage queuing of messages in TA instead of this sleep!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // For each adaptation
            for (IAdaptationModule am : toExecute) {
                currentModule = am;
                logger.info("Invoking Adaptation Module: " + am.getClass().getName());
                // Perform the adaptation
                am.apply(dac);


                vm.triggerAndReportValidation();
            }

        } catch (Exception e) {
            logger.error("Unexpected error invoking triggerAdaptation.", e);
            if (currentModule == null) {
                ImmortalsErrorHandler.reportFatalException(e);
            } else {
                ImmortalsErrorHandler.reportFatalException(e);
            }
        }

        logger.info("Adaptation process is complete.");
    }

    public String initialize() throws IOException {

        String graphUri = null;

        SimpleKnowledgeRepoClient client =
                new SimpleKnowledgeRepoClient();

        graphUri = client.ingest();

        return graphUri;

    }

    public String initializeKnowledgeRepo() {

        String byteCodeURI = null;
        Path ingestionPath = ImmortalsConfig.getInstance().globals.getTtlIngestionDirectory();

        try {
            repositoryService = ClientBuilder.newClient(new ClientConfig()
                    .register(JacksonFeature.class))
                    .target(REPOSITORY_SERVICE_CONTEXT_ROOT);
            byteCodeURI = repositoryService.path("ingest").request().post(
                    Entity.entity(ingestionPath.toString(), MediaType.TEXT_PLAIN), String.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (byteCodeURI != null && byteCodeURI.trim().length() > 0) {
            byteCodeURI = SparqlQuery.FUSEKI_DATA_ENDPOINT + byteCodeURI;
        }

        return byteCodeURI;

    }

    private static WebTarget repositoryService;
    private static AdaptationManager instance;
    private static final String REPOSITORY_SERVICE_CONTEXT_ROOT = "http://localhost:9999/krs/";

}
