package mil.darpa.immortals.core.das.adaptationmodules.protocolchanges;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import mil.darpa.immortals.das.ImmortalsProcessBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.darpa.immortals.config.ImmortalsConfig;
import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.AbstractAdaptationModule;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;

import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.core.das.sparql.deploymentmodel.DetermineLITLApplicability;
import mil.darpa.immortals.das.context.DasAdaptationContext;

public class ProtocolChangeAdapter extends AbstractAdaptationModule {

    private DasAdaptationContext context = null;
    private static final Logger logger = LoggerFactory.getLogger(ProtocolChangeAdapter.class);
    private static final String BUILD_TARGET = "ATAKLite";
    
	@Override
	public boolean isApplicable(DasAdaptationContext context) throws Exception {

		boolean result = false;
		
		result = DetermineLITLApplicability.select(context);
		
		return result;
	}

	@Override
	public void apply(DasAdaptationContext context) throws Exception {

        if (ImmortalsConfig.getInstance().debug.isUseMockDas()) {
            mockApply(context);
            return;
        }

        this.context = context;

        LinkedList<String> errorMessages = new LinkedList<>();
        LinkedList<String> detailMessages = new LinkedList<>();

        if (isApplicable(context)) {
        	
        	adaptProtocol();
        	
            DasOutcome outcome = null;

            try {
                build(context);
                outcome = DasOutcome.SUCCESS;
                String status = "Code sites analyzed and repaired.";
                detailMessages.add(status);
            } catch (Exception e) {
                outcome = DasOutcome.ERROR;
                String error = "Unexpected build error after ProtocolChangeAdapter is applied.";
                logger.error(error, e);
                errorMessages.add(error + " Message: " + e.getMessage());
            }
            
            AdaptationDetails ad = new AdaptationDetails(getClass().getName(), outcome, context.getAdaptationIdentifer());
            context.submitAdaptationStatus(ad);
        }
	}
	
	private int adaptProtocol() throws Exception {
		
		String modificationRoot = GradleKnowledgeBuilder.getBuildInstance(BUILD_TARGET,context.getAdaptationIdentifer()).getSourceRoot().toString();
		String analysisRoot = GradleKnowledgeBuilder.getBuildBase(BUILD_TARGET).getSourceRoot().toString();
		String locationAndroidJar = ImmortalsConfig.getInstance().build.augmentations.getAndroidSdkJarPath().toString();
				
		//Call PQL program with inputs
        Path immortalsRoot = ImmortalsConfig.getInstance().globals.getImmortalsRoot();
        String pqlScript = immortalsRoot.resolve("das/das-service/runpql.sh").toString();

        ImmortalsProcessBuilder pb = new ImmortalsProcessBuilder();
        pb.command(pqlScript, modificationRoot, analysisRoot, locationAndroidJar);
        Process p = null;
        int commandResult = 0;

        try {
            p = pb.start();
            if (p.waitFor(1, TimeUnit.MINUTES)) {
                commandResult = p.exitValue();
            } else {
                commandResult = -1;
                logger.info("PQL timed out.");
                //Need to kill it now
                ProcessBuilder pbkill = new ProcessBuilder("pkill", "-9", "-f", "java.*pql");
                pbkill.start();
            }
        } catch (InterruptedException | IOException e) {
            logger.error("PQL encountered an exception.", e);
        }
        
        return commandResult;
		
	}

    public void mockApply(DasAdaptationContext context) {
        try {

            AdaptationDetails starting = new AdaptationDetails(
                    getClass().getName(),
                    DasOutcome.RUNNING,
                    context.getAdaptationIdentifer()
            );
            context.submitAdaptationStatus(starting);
            Thread.sleep(1000);

            LinkedList<String> detailMessages = new LinkedList<>();
            detailMessages.add("Mock Success!");

            //AdaptationDetails update = starting.produceUpdate(DasOutcome.SUCCESS, new LinkedList<>(), detailMessages);
            //context.submitAdaptationStatus(update);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void build(DasAdaptationContext dac) throws Exception {

        AdaptationTargetBuildInstance atakLiteInstance =
                GradleKnowledgeBuilder.getBuildInstance(BUILD_TARGET,
                        dac.getAdaptationIdentifer());

        // Build and publish the new ATAKLite instance
        atakLiteInstance.executeCleanAndBuild();

    }    
}
