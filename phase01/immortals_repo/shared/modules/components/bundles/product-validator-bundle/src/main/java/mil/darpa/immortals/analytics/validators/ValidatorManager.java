package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.L;
import mil.darpa.immortals.analytics.validators.result.ValidationResults;
import mil.darpa.immortals.analytics.validators.result.ValidationResultsListener;
import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 10/21/16.
 */
public class ValidatorManager {

    private boolean isValidating = true;

    private long testStartTimeMS;

    public final ValidationResultsListener finishedListener;

    public final HashSet<ValidatorInterface> runningValidators = new HashSet<>();
    public final LinkedList<ValidatorResult> finishedValidators = new LinkedList<>();


    public ValidatorManager(ValidationResultsListener finishedListener) {
        this.finishedListener = finishedListener;
        this.testStartTimeMS = System.currentTimeMillis();
    }

    public synchronized void forceEndValidation() {
        if (runningValidators != null && !runningValidators.isEmpty()) {
            for (ValidatorInterface running : runningValidators) {
                finishedValidators.add(running.attemptValidation());
            }
            runningValidators.clear();
        }
        endValidation();
    }

    public synchronized void addValidator(Set<String> clientIdentifiers, String validatorIdentifier) {

        if (validatorIdentifier == null) {
            throw new RuntimeException("No validator provided!");

        } else if (!Validators.getValidatorIdentifierList().contains(validatorIdentifier)) {
            String str = "Unexpected validator identifier '" + validatorIdentifier + "'! Valid validators:";

            for (String identifier : Validators.getValidatorIdentifierList()) {
                str += "\n\t" + identifier;
            }
            throw new RuntimeException(str);
        } else {
            runningValidators.add(Validators.getByLabel(validatorIdentifier).construct(clientIdentifiers));
        }
    }

    public final LinkedList<ValidatorInterface> validatorRemovalQueue = new LinkedList<>();

    public synchronized void processEvent(AnalyticsEvent event) {
        L.analyticsEvent(event);
        for (ValidatorInterface validator : runningValidators) {
            validator.processEvent(event);
            ValidatorResult result = validator.attemptValidation();
            if (result.currentState == ValidatorState.PASSED || result.currentState == ValidatorState.FAILED) {
                finishedValidators.add(result);
                validatorRemovalQueue.add(validator);
            }
        }

        for (ValidatorInterface validator : validatorRemovalQueue) {
            runningValidators.remove(validator);
        }

        if (runningValidators.isEmpty()) {
            endValidation();
        }
    }

    public synchronized void endValidation() {
        if (isValidating) {
            isValidating = false;
            long elapsedTime = System.currentTimeMillis() - testStartTimeMS;
            ValidationResults results = new ValidationResults(finishedValidators, elapsedTime);
            L.validationFinished(results);
            finishedListener.finished(results);
        }
    }
}
