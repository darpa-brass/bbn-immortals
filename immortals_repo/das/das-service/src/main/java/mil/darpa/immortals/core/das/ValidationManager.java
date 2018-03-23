package mil.darpa.immortals.core.das;

import mil.darpa.immortals.core.api.ll.phase2.result.AdaptationDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.TestDetails;
import mil.darpa.immortals.core.api.ll.phase2.result.status.DasOutcome;
import mil.darpa.immortals.core.api.ll.phase2.result.status.TestOutcome;
import mil.darpa.immortals.core.das.adaptationmodules.IAdaptationModule;
import mil.darpa.immortals.core.das.adaptationmodules.schemaevolution.SchemaEvolutionAdapter;
import mil.darpa.immortals.core.das.adaptationtargets.building.AdaptationTargetBuildInstance;
import mil.darpa.immortals.core.das.knowledgebuilders.building.GradleKnowledgeBuilder;
import mil.darpa.immortals.das.context.DasAdaptationContext;
import mil.darpa.immortals.das.context.ImmortalsErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by awellman@bbn.com on 3/21/18.
 */
public class ValidationManager {
    
    Logger logger = LoggerFactory.getLogger(ValidationManager.class);

    private final DasAdaptationContext dac;

    public ValidationManager(DasAdaptationContext dac) {
        this.dac = dac;
    }

    private LinkedList<TestDetails> validators;
    TestDetails schemaEvolutionTestDetails;

    public synchronized List<TestDetails> queryAndReportValidatiors() {
        if (validators == null) {
            validators = new LinkedList<>();
        }
        // Submit initial test details
        // TODO: Determine this programatically. Will be needed for CP3
        schemaEvolutionTestDetails = new TestDetails(
                "SchemaValidationTest",
                TestOutcome.PENDING,
                dac.getAdaptationIdentifer()
        );
        validators.add(schemaEvolutionTestDetails);
        dac.submitValidationStatus(schemaEvolutionTestDetails);
        
        return validators;
    }

    public synchronized void triggerAndReportValidation() {
        if (validators == null) {
            queryAndReportValidatiors();
        }

        try {
            // TODO: Manage queuing of messages in TA instead of this sleep!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            dac.submitValidationStatus(schemaEvolutionTestDetails.produceUpdate(TestOutcome.RUNNING));

            // TODO: Manage queuing of messages in TA instead of this sleep!
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            AdaptationTargetBuildInstance takServerDataManagerInstance =
                    GradleKnowledgeBuilder.getBuildInstance("TakServerDataManager",
                            dac.getAdaptationIdentifer());

            boolean validationSuccessful = takServerDataManagerInstance.executeCleanAndTest();

            dac.submitValidationStatus(schemaEvolutionTestDetails.produceUpdate(
                    validationSuccessful ? TestOutcome.COMPLETE_PASS : TestOutcome.COMPLETE_FAIL,
                    validationSuccessful ? null : "Validation failed",
                    validationSuccessful ? "Validation successful" : null
            ));

        } catch (Exception e) {
            logger.error("Unexpected error invoking triggerAndReportValidation.", e);
                ImmortalsErrorHandler.reportFatalError("Unexpected error with validation module: " + e.getMessage());
        }
    }
}
