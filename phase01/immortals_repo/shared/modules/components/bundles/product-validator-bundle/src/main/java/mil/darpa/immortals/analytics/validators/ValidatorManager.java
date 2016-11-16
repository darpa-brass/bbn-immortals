package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.L;
import mil.darpa.immortals.analytics.validators.result.*;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;

import java.util.*;

/**
 * Created by awellman@bbn.com on 10/21/16.
 */
public class ValidatorManager {

    public static final List<String> VALIDATOR_IDENTIFIERS;

    private boolean isValidating = true;

    private long testStartTimeMS;

    static {
        LinkedList<String> tmp = new LinkedList();
        tmp.add("client-location-share");
        tmp.add("client-location-produce");
        tmp.add("client-location-trusted");
        tmp.add("client-image-share");
        tmp.add("client-image-produce");

        VALIDATOR_IDENTIFIERS = Collections.unmodifiableList(tmp);
    }

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

        } else if (!VALIDATOR_IDENTIFIERS.contains(validatorIdentifier)) {
            String str = "Unexpected validator identifier '" + validatorIdentifier + "'! Valid validators:";

            for (String identifier : VALIDATOR_IDENTIFIERS) {
                str += "\n\t" + identifier;
            }
            throw new RuntimeException(str);
        } else {

            ValidatorInterface validator;

            switch (validatorIdentifier) {

                case "client-location-share":
                    validator = new ClientLocationShareValidator(clientIdentifiers);
                    break;

                case "client-location-produce":
                    validator = new ClientLocationUpdateValidator(clientIdentifiers);
                    break;

                case "client-image-share":
                    validator = new ClientImageShareValidator(clientIdentifiers);
                    break;

                case "client-image-produce":
                    validator = new ClientImageProduceValidator(clientIdentifiers);
                    break;

                case "client-location-trusted":
                    validator = new TrustedLocationValidator(clientIdentifiers);
                    break;

                default:
                    throw new RuntimeException("ERROR: The specified identifier '" + validatorIdentifier + "' is defined but not set to anything!");
            }

            runningValidators.add(validator);
        }
    }

    public final LinkedList<ValidatorInterface> validatorRemovalQueue = new LinkedList<>();
    public synchronized void processEvent(AnalyticsEvent event) {
        L.analyticsEvent(event);
        for(ValidatorInterface validator : runningValidators) {
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
