package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.analytics.validators.result.ValidatorResult;
import mil.darpa.immortals.analytics.validators.result.ValidatorState;
import mil.darpa.immortals.core.analytics.AnalyticsEvent;
import mil.darpa.immortals.core.analytics.AnalyticsEventType;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * Created by awellman@bbn.com on 11/11/16.
 */
public abstract class AbstractClientEventValidator implements ValidatorInterface {

    private ValidatorResult state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, null);

    private final HashSet<String> expectedClients;

    private final HashMap<String, Boolean> clientUpdateMap = new HashMap<>();

    public AbstractClientEventValidator(@Nonnull Set<String> clientIdentifiers) {
        expectedClients = new HashSet<>(clientIdentifiers);
    }

    @Override
    public synchronized void processEvent(AnalyticsEvent event) {
        if (event.type == getDesiredEventType()) {
            clientUpdateMap.put(event.eventSource, true);
        }
    }

    protected abstract AnalyticsEventType getDesiredEventType();

    @Override
    public synchronized ValidatorResult attemptValidation() {
        if (state.currentState == ValidatorState.RUNNING) {
            LinkedList<String> validationErrors = new LinkedList<>();

            if (expectedClients.size() != clientUpdateMap.keySet().size()) {
                validationErrors.add(clientUpdateMap.keySet().size() + "/" + expectedClients.size() + " of expected clients came online!");

            } else {
                for (String clientIdentifier : expectedClients) {
                    if (!(clientUpdateMap.containsKey(clientIdentifier) && clientUpdateMap.get(clientIdentifier))) {
                        validationErrors.add(clientIdentifier + " has not produced a " + getDesiredEventType().name() + "event!");
                    }
                }
            }


//            if (clientLocationUpdatedMap.size() != expectedClientCount) {
//                validationErrors.add("Only " + clientLocationUpdatedMap.size() + "/" + expectedClientCount + " clients have come online.");
//
//            } else {
//                for (String clientIdentifier : clientLocationUpdatedMap.keySet()) {
//                    if (!clientLocationUpdatedMap.get(clientIdentifier)) {
//                        validationErrors.add(clientIdentifier + " has not produced a location!");
//                    }
//                }
//            }
            if (validationErrors.isEmpty()) {
                ValidatorResult vs = new ValidatorResult(getValidatorName(), ValidatorState.PASSED, null);
                state = vs;
                return vs;

            } else {
                state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, validationErrors);
            }
        }
        return state;
    }

    public String toString() {
        return getValidatorName();
    }
}
