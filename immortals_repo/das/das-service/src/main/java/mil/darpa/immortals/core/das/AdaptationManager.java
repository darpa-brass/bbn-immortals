package mil.darpa.immortals.core.das;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetailsList;
import mil.darpa.immortals.core.das.adaptationmodules.hddrass.HddRassAdapter;
import mil.darpa.immortals.core.api.TestCaseReportSet;
import mil.darpa.immortals.core.das.upgrademodules.LibraryUpgradeModule;
import mil.darpa.immortals.core.das.upgrademodules.UpgradeModuleInterface;
import mil.darpa.immortals.das.context.ContextManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationmodules.schemaevolution.SchemaEvolutionAdapter;
import mil.darpa.immortals.core.das.sparql.SparqlQuery;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;

public class AdaptationManager {

    static final Logger logger = LoggerFactory.getLogger(AdaptationManager.class);
    private List<IAdaptationModule> adaptationModules = new ArrayList<>();
    private List<UpgradeModuleInterface> upgradeModules = new ArrayList<>();
    
    static {
        instance = new AdaptationManager();
    }

    private AdaptationManager() {
        upgradeModules.add(new LibraryUpgradeModule());
        
        adaptationModules.add(new HddRassAdapter());
        adaptationModules.add(new SchemaEvolutionAdapter());
    }

    public static AdaptationManager getInstance() {
        return instance;
    }

    public void triggerAdaptation(DasAdaptationContext dac) {

        IAdaptationModule currentModule = null;
        try {
            // First, perform any upgrades if necessary
            for (UpgradeModuleInterface upgradeModule : upgradeModules) {
                if (upgradeModule.isApplicable(dac)) {
                    upgradeModule.apply(dac);
                }
            }
            
            List<IAdaptationModule> toExecute = new LinkedList<>();

            // Then attempt to execute all tests from applicable adaptation targets in the deployment model to get
            // an idea of the current system state.
            ValidationManager vm = new ValidationManager(dac);
            TestCaseReportSet initialTestReports = vm.executeValidation(false);


            // Then save them in the context manager for use by adaptation modules
            ContextManager.setAdaptationTargetState(dac, initialTestReports);

            // Followed by submitting the test details to the Test Adapter
            TestDetailsList td = TestDetailsList.fromTestCaseReportSet(dac.getAdaptationIdentifer(), initialTestReports);
            dac.submitValidationStatus(td);
            
            // TODO: Manage queuing of messages in TA instead of this sleep!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Then, determine which adaptation modules will be run
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
                
                // Submit the RUNNING status update
                AdaptationDetails ad = new AdaptationDetails(
                        am.getClass().getName(),
                        DasOutcome.RUNNING, dac.getAdaptationIdentifer());
                dac.submitAdaptationStatus(ad);
                
                
                // Perform the adaptation
                am.apply(dac);
                
                // And attempt another round of validation
                TestCaseReportSet testReports = vm.executeValidation(true);
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

        logger.debug("Submitting '" + ingestionPath.toString() + "' to the repository service.");

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
            logger.debug("Received URI '" + byteCodeURI + "' from the repository service.");
        }

        return byteCodeURI;

    }

    private static WebTarget repositoryService;
    private static AdaptationManager instance;
    private static final String REPOSITORY_SERVICE_CONTEXT_ROOT = ImmortalsConfig.getInstance().knowledgeRepoService.getFullUrl().resolve("/krs/").toString();

}
