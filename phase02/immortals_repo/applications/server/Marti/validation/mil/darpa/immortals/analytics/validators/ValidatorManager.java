package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.analytics.*;
import mil.darpa.immortals.core.api.validation.ValidationStartData;
import mil.darpa.immortals.core.api.validation.results.ValidationResults;
import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.ll.phase1.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by awellman@bbn.com on 10/21/16.
 */
public class ValidatorManager implements AnalyticsEndpointInterface {

    private Logger logger = LoggerFactory.getLogger(ValidatorManager.class);

    public final HashSet<ValidatorInterface> validators = new HashSet<>();

//    private final Analytics analytics;

    private final boolean haltOnSuccessfulValidation;

    @Override
    public void start() {
        testStartTimeMS = System.currentTimeMillis();
        for (ValidatorInterface vi : validators) {
            vi.start();
        }
    }

    private boolean isValidating = true;

    private long testStartTimeMS;

    private final LinkedList<TestResult> finishedValidators = new LinkedList<>();

    private EndpointSender<ValidationResults> endpointSender;
    
    private ValidationResults results;

    public ValidatorManager(ValidationStartData startData) {
        this(startData.clientIdentifiers, startData.validatorIdentifiers, true);

        if (logger.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder("Created ValidatorManager Instance with the following data:");
            sb.append("\n\tclientIdentifiers: ").append(startData.clientIdentifiers.toString());
            sb.append("\n\tvalidatorIdentifiers: ").append(startData.validatorIdentifiers.toString());
            sb.append("\n\t maxRuntimeMS").append(startData.maxRuntimeMS);
            sb.append("\n\tminRuntimeMS").append(startData.minRuntimeMS);
            logger.debug(sb.toString());
        }
        logger.info("ValidatorManager: Created");
    }

    public ValidatorManager(Collection<String> clientIdentifiers, Collection<String> validatorIdentifiers, boolean haltOnSuccessfulValidation) {
//        Analytics.registerCurrentThread();
        
//        analytics = Analytics.getInstance();
        this.haltOnSuccessfulValidation = haltOnSuccessfulValidation;

        if (validatorIdentifiers.contains("all")) {
            validatorIdentifiers = new ArrayList<>(Validators.getValidatorIdentifierList());
        }

        HashSet<String> clids = new HashSet<>(clientIdentifiers);
        for (String validatorIdentifier : validatorIdentifiers) {
            addValidator(clids, validatorIdentifier);
        }
    }

    public ValidatorManager setEndpointSender(EndpointSender<ValidationResults> endpointSender) {
        this.endpointSender = endpointSender;
        return this;
    }

    @Override
    public synchronized void shutdown() {

        if (!validators.isEmpty()) {
            for (ValidatorInterface running : validators) {
                finishedValidators.add(running.attemptValidation(true));
            }
            validators.clear();
        }
        endValidation();
        logger.info("ValidatorManager: Shutdown");
    }
    
//    public synchronized void attempt_validation() {
//        if (!validators.isEmpty()) {
//            LinkedList<TestResult> lFinishedValidators = new LinkedList<>();
//            for (ValidatorInterface running : validators) {
//                TestResult vr = running.attemptValidation(false);
//                if (vr.currentState == Status.SUCCESS) {
//                    lFinishedValidators.add(vr);
//                } else {
//                    return;
//                }
//            }
//            if (lFinishedValidators.size() == validators.size()) {
//                validators.clear();
//                endValidation();
//            }
//        }
//    }

    private synchronized ValidatorManager addValidator(Set<String> clientIdentifiers, String validatorIdentifier) {

        if (validatorIdentifier == null) {
            throw new RuntimeException("No validator provided!");

        } else if (!Validators.getValidatorIdentifierList().contains(validatorIdentifier)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected validator identifier '").append(validatorIdentifier).append("'! Valid validators:");

            for (String identifier : Validators.getValidatorIdentifierList()) {
                sb.append("\n\t").append(identifier);
            }
            throw new RuntimeException(sb.toString());
        } else {
            validators.add(Validators.byIdentifier(validatorIdentifier).construct(clientIdentifiers, haltOnSuccessfulValidation));
        }
        return this;
    }

    @Override
    public synchronized void log(AnalyticsEvent event) {
//        analytics.logEvent(event);
        LinkedList<ValidatorInterface> validatorRemovalQueue = new LinkedList<>();

        for (ValidatorInterface validator : validators) {
            validator.processEvent(event);
            TestResult result = validator.attemptValidation(false);
            if (result.currentState == Status.SUCCESS || result.currentState == Status.FAILURE) {
                finishedValidators.add(result);
                validatorRemovalQueue.add(validator);
            }
        }

        for (ValidatorInterface validator : validatorRemovalQueue) {
            validators.remove(validator);
        }

        if (validators.isEmpty()) {
            endValidation();
        }
    }

    private synchronized void endValidation() {
        if (isValidating) {
            isValidating = false;
            long elapsedTime = System.currentTimeMillis() - testStartTimeMS;
            results = new ValidationResults(finishedValidators, elapsedTime);
            if (endpointSender != null) {
                endpointSender.send(results);
            }
//            analytics.logEvent(Analytics.newEvent(AnalyticsEventType.Tooling_ValidationFinished, results));
        }
    }
    
    public ValidationResults getResults() {
        return this.results;
    }
}
