package mil.darpa.immortals.analytics.validators;

import mil.darpa.immortals.core.api.ll.phase1.TestResult;
import mil.darpa.immortals.core.api.ll.phase1.Status;
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

    private TestResult state = new TestResult(getValidatorIdentifier(), Status.PENDING, null, null);

    private final HashSet<String> expectedClients;

    private final HashMap<String, Boolean> clientUpdateMap = new HashMap<>();

    AbstractClientEventValidator(@Nonnull Set<String> clientIdentifiers, boolean haltUponInitialValidation) {
        expectedClients = new HashSet<>(clientIdentifiers);
    }

    @Override
    public synchronized void processEvent(AnalyticsEvent event) {
        
        if (state.currentState == Status.RUNNING && event.type == getDesiredEventType()) {
            clientUpdateMap.put(event.eventSource, true);
        }
    }

    @Override
    public String getValidatorIdentifier() {
        return Validators.byClass(this.getClass()).identifier;
    }

    protected abstract AnalyticsEventType getDesiredEventType();

    @Override
    public synchronized TestResult attemptValidation(boolean terminalState) {
        if (state.currentState == Status.RUNNING) {
            LinkedList<String> validationErrors = new LinkedList<>();
            LinkedList<String> detailMessages = new LinkedList<>();

            if (expectedClients.size() != clientUpdateMap.keySet().size()) {
                validationErrors.add(clientUpdateMap.keySet().size() + "/" + expectedClients.size() + " of expected clients came online!");

            } else {
                for (String clientIdentifier : expectedClients) {
                    if (!clientUpdateMap.containsKey(clientIdentifier) || !clientUpdateMap.get(clientIdentifier)) {
                        validationErrors.add(clientIdentifier + " has not produced a " + getDesiredEventType().name() + "event!");
                    } else {
                        detailMessages.add(clientIdentifier + "-" + getDesiredEventType().name() + "->");
                    }
                }
            }

            if (validationErrors.isEmpty()) {
                TestResult vs = new TestResult(getValidatorIdentifier(), Status.SUCCESS, validationErrors, detailMessages);
                state = vs;
                return vs;

            } else if (terminalState) {
                TestResult vs = new TestResult(getValidatorIdentifier(), Status.FAILURE, validationErrors, detailMessages);
                state = vs;
                return vs;

            } else {
                state = new TestResult(getValidatorIdentifier(), Status.RUNNING, validationErrors, detailMessages);
            }
        }
        return state;
    }

    @Override
    public void start() {
        state = new TestResult(getValidatorIdentifier(), Status.RUNNING, null, null);
    }

    public String toString() {
        return getValidatorIdentifier();
    }
}
