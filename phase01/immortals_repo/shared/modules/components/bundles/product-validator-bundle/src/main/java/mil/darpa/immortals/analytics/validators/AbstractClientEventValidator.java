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
 * Abstract validator for events produced by all clients
 * <p>
 * Created by awellman@bbn.com on 11/11/16.
 */
abstract class AbstractClientEventValidator implements ValidatorInterface {

    private ValidatorResult state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, null, null);

    private final HashSet<String> expectedClients;

    private final HashMap<String, Boolean> clientUpdateMap = new HashMap<>();

    AbstractClientEventValidator(@Nonnull Set<String> clientIdentifiers) {
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
            LinkedList<String> detailMessages = new LinkedList<>();

            if (expectedClients.size() != clientUpdateMap.keySet().size()) {
                validationErrors.add(clientUpdateMap.keySet().size() + "/" + expectedClients.size() + " of expected clients came online!");

            } else {
                for (String clientIdentifier : expectedClients) {
                    if (!(clientUpdateMap.containsKey(clientIdentifier) && clientUpdateMap.get(clientIdentifier))) {
                        validationErrors.add(clientIdentifier + " has not produced a " + getDesiredEventType().name() + "event!");
                    } else {
                        detailMessages.add(clientIdentifier + "-" + getDesiredEventType().name() + "->");
                    }
                }
            }

            if (validationErrors.isEmpty()) {
                ValidatorResult vs = new ValidatorResult(getValidatorName(), ValidatorState.PASSED, validationErrors, detailMessages);
                state = vs;
                return vs;

            } else {
                state = new ValidatorResult(getValidatorName(), ValidatorState.RUNNING, validationErrors, detailMessages);
            }
        }
        return state;
    }

    public String toString() {
        return getValidatorName();
    }
}
